//package es.uv.twcam.framework;

import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.io.*;

public class GeneraCodigo {
    private static void startClassStub(PrintWriter pw, String interfaceName) {
        String className = interfaceName + "_stub";
        pw.println("import java.net.*;");
        pw.println("import java.io.*;");
        pw.println("public class " + className + " implements " + interfaceName + "{");
        pw.println("   private String host;");
        pw.println("   private int port;");
        pw.println("   public " + className + "(String host, int port){");
        pw.println("      this.host = host;");
        pw.println("      this.port = port;");
        pw.println("   }");
    }

    private static void generateMethodStub(PrintWriter pw, Method m) {
        String methodName = m.getName();
        pw.print("   public ");
        Type returnType = m.getGenericReturnType();

        pw.print(returnType.getTypeName());
        pw.print(" " + methodName + "(");

        Type[] parameterTypes = m.getGenericParameterTypes();
        int counter = 0;
        StringBuffer sb = new StringBuffer();
        for (Type type : parameterTypes) {
            sb.append(type.getTypeName());
            sb.append(' ');
            sb.append("arg" + counter);
            sb.append(',');
            counter++;
        }
        if (sb.length()>0)
           sb.deleteCharAt(sb.length() - 1);
       sb.append(") throws RemoteException{");
       pw.println(sb.toString());        
       pw.println("   try{");
       pw.println("   Socket s = new Socket(host,port);");
       pw.println("   ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());");

       pw.println("   out.writeObject(\"" + methodName + "\");");
       pw.println("   out.flush();");
       for (int i = 0; i < parameterTypes.length; i++)
        pw.println("   out.writeObject(arg" + i + ");");
    pw.println("   out.flush();");
    pw.println("   out.close();");
    if (returnType != Void.TYPE) {
        pw.println("   ObjectInputStream in = new ObjectInputStream(s.getInputStream());");
        pw.println("   "+ returnType.getTypeName() + " ret  = (" + returnType.getTypeName() + ")in.readObject();");
        pw.println("   in.close();");
    }
    pw.println("s.close();");
    if (returnType != Void.TYPE) 
       pw.println("return ret;");
   pw.println("}catch(Exception ex){");
   pw.println("   throw new RemoteException(ex.toString());");
   pw.println("}");

   pw.println("}");

}

private static void closeClassStub(PrintWriter pw) {
    pw.println("}");
    pw.flush();
    pw.close();
}

private static void createSkeletonClass(PrintWriter pw, String interfaceName) {
    String className = interfaceName + "_skeleton";

    pw.println("import java.io.IOException;");
    pw.println("import java.io.ObjectInputStream;");
    pw.println("import java.io.ObjectOutputStream;");
    pw.println("import java.lang.reflect.InvocationTargetException;");
    pw.println("import java.lang.reflect.Method;");
    pw.println("import java.lang.reflect.Type;");
    pw.println("import java.net.ServerSocket;");
    pw.println("import java.net.Socket;");
    pw.println("import java.util.ArrayList;");
    pw.println("import java.util.concurrent.ExecutorService;");
    pw.println("import java.util.concurrent.Executors;");

    pw.println("public class " + className + "{");
    pw.println("   private  int port;");
    pw.println("   private ExecutorService es;");
    pw.println("   private "+ interfaceName+" vmi;");
    pw.println("   private int threads;");

    pw.println("   public " + className + "(" + interfaceName + " vmi, int myPort, int threads){");
    pw.println("      this.vmi = vmi;");
    pw.println("      port = myPort;");
    pw.println("      this.threads = threads;");
    pw.println("      es = Executors.newFixedThreadPool(threads);");
    pw.println("   }");
    pw.println("public void start() {");
    pw.println("try {");
    pw.println("    ServerSocket server = new ServerSocket(port);");
    pw.println("   while(true) {");
    pw.println("       Socket s = server.accept();");
    pw.println("       ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());");
    pw.println("       ObjectInputStream in = new ObjectInputStream(s.getInputStream());");
    pw.println("       es.execute(new ImplementsClient(vmi, in, out));");
    pw.println("   }");
    pw.println("   } catch (IOException | SecurityException  | IllegalArgumentException  e) {");
    pw.println("      e.printStackTrace();");
    pw.println("   }");
    pw.println("}");
    pw.println("}");
}

public static void createRunnableClass(PrintWriter pw, String interfaceName){


    pw.println("class ImplementsClient implements Runnable {");
    pw.println("private ObjectInputStream in;");
    pw.println("private ObjectOutputStream out;");
    pw.println("private " + interfaceName + " vmi;");
    pw.println("public ImplementsClient("+interfaceName+" vmi, ObjectInputStream in, ObjectOutputStream out ){");
    pw.println("    this.in = in;");
    pw.println("    this.out = out;");
    pw.println("    this.vmi = vmi;");
    pw.println("}");

    pw.println("@Override");
    pw.println("public void run() { ");
    pw.println("    Class<?> myClass;");
    pw.println("    try {");
    pw.println("        myClass = Class.forName(vmi.getClass().toString().split(\" \")[1]);");
    pw.println("        ArrayList<Object> array = new ArrayList<Object>();");
    pw.println("        String method = (String)in.readObject();");
    pw.println("        for(Method m : myClass.getMethods()) {");
    pw.println("          if(m.getName().equals(method)) {");
    pw.println("             Type return_type = m.getGenericReturnType();");
    pw.println("             for(Type types: m.getGenericParameterTypes()) {");
    pw.println("                 array.add(in.readObject());");
    pw.println("             }");
    pw.println("             if (return_type.toString().equals(\"void\")) {");
    pw.println("                 m.invoke(vmi, array.toArray());");
    pw.println("             }else {");
    pw.println("                Object obj = m.invoke(vmi, array.toArray());");
    pw.println("                out.writeObject(obj);");
    pw.println("             }");
    pw.println("           break;");
    pw.println("            }");
    pw.println("            }");
    pw.println("         } catch (ClassNotFoundException | IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {");
    pw.println("              e.printStackTrace();");
    pw.println("         }");
    pw.println("    }");
    pw.println("}");
    pw.flush();
    pw.close();

}

public static void main(String[] args) {
    try {
        String interfaceName = args[0];
        String className = interfaceName + "_stub";
        Class<?> c = Class.forName(interfaceName);
        PrintWriter pw = new PrintWriter(new FileWriter(className + ".java"));
        startClassStub(pw, interfaceName);
        Method[] methods = c.getMethods();
        for (Method m : methods)
            generateMethodStub(pw, m);
        closeClassStub(pw);

        className = interfaceName + "_skeleton";
        PrintWriter pwSkeleton = new PrintWriter(new FileWriter(className + ".java"));
        createSkeletonClass(pwSkeleton, interfaceName);
        createRunnableClass(pwSkeleton, interfaceName);

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
}
