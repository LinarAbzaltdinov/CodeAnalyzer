package ru.abzaltdinov;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Main {

    private static PrintWriter printWriter;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("You should set ONLY ONE path to: source .java file / Maven .pom file");
            return;
        }
        initPrintWriter();
        boolean isJavaFile = args[0].endsWith("java");
        Launcher launcher;
        if (isJavaFile) {
            launcher = new Launcher();
            launcher.addInputResource(args[0]);
        } else {
            launcher = new MavenLauncher(args[0], MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        }
        launcher.getEnvironment().setAutoImports(true); // optional
        launcher.getEnvironment().setNoClasspath(true); // optional
        launcher.buildModel();
        CtModel model = launcher.getModel();
        printWriter.println("Expected NullPointerExceptions(with stack trace):");
        List<CtExecutable<?>> executables = model.getElements(new TypeFilter<>(CtExecutable.class));
        executables.forEach(exec -> check(exec, new ArrayList<>(), new ArrayList<>()));
        printWriter.close();
    }

    private static void initPrintWriter() throws IOException {
        File outFile = new File("analyzeResults.txt");
        outFile.createNewFile();
        printWriter = new PrintWriter(outFile);
    }

    public static void check(CtExecutable<?> ctExecutable,
                             ArrayList<Integer> indexesOfNullArgs,
                             ArrayList<CtStatement> stackTrace) {
        CtBlock<?> body = ctExecutable.getBody();
        if (body == null) {
            stackTrace.forEach(System.out::println);
            System.out.println();
            return;
        }

        HashSet<CtVariableReference<?>> nullVars = new HashSet<>();
        for (Integer indexOfNull : indexesOfNullArgs) {
            nullVars.add(ctExecutable.getParameters().get(indexOfNull).getReference());
        }

        for (CtStatement ctStatement : body) {
            if (ctStatement instanceof CtInvocation<?>) {
                stackTrace.add(ctStatement);
                CtInvocation<?> invocation = (CtInvocation<?>) ctStatement;
                CtExpression<?> target = invocation.getTarget();
                if (target instanceof CtVariableAccess<?> && nullVars.contains(((CtVariableAccess<?>) target).getVariable())) {
                    printNPEWarning(stackTrace);
                }
                ArrayList<Integer> indexesOfNullArgsInMethodInvocation = new ArrayList<>();
                List<CtExpression<?>> arguments = invocation.getArguments();
                for (int i = 0; i < arguments.size(); ++i) {
                    CtExpression<?> arg = arguments.get(i);
                    if (arg == null || isExpressionIsNull(arg, nullVars)) {
                        indexesOfNullArgsInMethodInvocation.add(i);
                    }
                }
                if (!indexesOfNullArgsInMethodInvocation.isEmpty()) {
                    check(invocation.getExecutable().getExecutableDeclaration(),
                            indexesOfNullArgsInMethodInvocation,
                            stackTrace);
                }
                stackTrace.remove(stackTrace.size() - 1);
            } else if (ctStatement instanceof CtVariable<?>) {
                CtVariable<?> currVar = (CtVariable) ctStatement;
                CtExpression<?> currVarDefaultExpression = currVar.getDefaultExpression();
                if (currVarDefaultExpression == null && !currVar.getType().isPrimitive()
                        || isExpressionIsNull(currVarDefaultExpression, nullVars)) {
                    nullVars.add(currVar.getReference());
                } else {
                    nullVars.remove(currVar.getReference());
                }
            }
        }
    }

    public static boolean isExpressionIsNull(CtExpression<?> ctExpression,
                                             HashSet<CtVariableReference<?>> nullVars) {
        return ctExpression instanceof CtLiteral<?> && ((CtLiteral<?>) ctExpression).getValue() == null
                || ctExpression instanceof CtVariableAccess<?>
                   && nullVars.contains(((CtVariableAccess<?>) ctExpression).getVariable());
    }

    public static void printNPEWarning(ArrayList<CtStatement> trace) {
        printWriter.println();
        for (CtStatement traceElement : trace) {
            SourcePosition position = traceElement.getPosition();
            printWriter.println(traceElement);
            String line = position + ", columns " + position.getColumn() + "-" + position.getEndColumn();
            printWriter.println(line);
        }
    }
}
