# Gradle API evolution demo

This is a playground repo to test out ideas around allowing code compiled against an older version of Gradle to run with a newer version of Gradle that have breaking changes.

## Structure

There are the following subprojects:

- `old-api` defines an old version of the `Server` type written in Java,
- `new-api` defines the new version of the `Server` type, also written in Java,
- `old-client` contains client code compiled against the old version of `Server`; there are clients written in Java, Kotlin and static and dynamic Groovy,
- `app` has the upgrade logic and a test that tries to run each of the clients in `old-client` against the `new-api`.

Try with:

```shell
$ ./gradlew check
```

## Property upgrade bytecode

## Java/Kotlin/static Groovy

```diff
diff --git a/javaold b/javanew
index 02f1620..0c43e4c 100644
--- a/javaold
+++ b/javanew
@@ -11,15 +11,16 @@ Label label1 = new Label();
 methodVisitor.visitLabel(label1);
 methodVisitor.visitLineNumber(6, label1);
 methodVisitor.visitVarInsn(ALOAD, 0);
+methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/gradle/demo/api/evolution/Server", "getName", "()Lorg/gradle/demo/api/evolution/Property;", false);
 methodVisitor.visitLdcInsn("lajos");
-methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/gradle/demo/api/evolution/Server", "setName", "(Ljava/lang/String;)V", false);
+methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/gradle/demo/api/evolution/Property", "set", "(Ljava/lang/Object;)V", false);
 Label label2 = new Label();
 methodVisitor.visitLabel(label2);
 methodVisitor.visitLineNumber(7, label2);
 methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
 methodVisitor.visitVarInsn(ALOAD, 0);
-methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/gradle/demo/api/evolution/Server", "getName", "()Ljava/lang/String;", false);
-methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
+methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/gradle/demo/api/evolution/Server", "getName", "()Lorg/gradle/demo/api/evolution/Property;", false);
+methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
 Label label3 = new Label();
 methodVisitor.visitLabel(label3);
 methodVisitor.visitLineNumber(8, label3);
```

## Dynamic Groovy

* Full filtered diff: https://gist.github.com/lptr/cd7c9e149e2db5d2c92df2cb6d709728
* Full unfiltered diff: https://gist.github.com/lptr/a7a8557be684978a553ae3d56ae73006

### Old code

```groovy
    private static void doSet(Object server) {
        server.setTestProperty("lajos")
    }
    private static Object doGet(Object server) {
        return server.getTestProperty()
    }
```

### New code

```groovy
    private static void doSet(Object server) {
        server.getTestProperty().set("lajos")
    }
    private static Object doGet(Object server) {
        return server.getTestProperty().get()
    }
```

### Get

```diff
   private static java.lang.Object doGet(java.lang.Object);
     descriptor: (Ljava/lang/Object;)Ljava/lang/Object;
     flags: (0x000a) ACC_PRIVATE, ACC_STATIC
     Code:
-      stack=2, locals=2, args_size=1
+      stack=3, locals=2, args_size=1
         : nop
         : invokestatic  #_                 // Method $getCallSiteArray:()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
         : astore_1
         : aload_1
-        : ldc           #_                 // int 5
+        : ldc           #_                 // int 6
+        : aaload
+        : aload_1
+        : ldc           #_                 // int 7
         : aaload
         : aload_0
         : invokeinterface #_,  2           // InterfaceMethod org/codehaus/groovy/runtime/callsite/CallSite.call:(Ljava/lang/Object;)Ljava/lang/Object;
+        : invokeinterface #_,  2           // InterfaceMethod org/codehaus/groovy/runtime/callsite/CallSite.call:(Ljava/lang/Object;)Ljava/lang/Object;
         : areturn
       LineNumberTable:
         line 14: 5
       LocalVariableTable:
         Start  Length  Slot  Name   Signature
-            0      16     0 server   Ljava/lang/Object;
+            0      25     0 server   Ljava/lang/Object;
```

### Set

```diff
   private static void doSet(java.lang.Object);
     descriptor: (Ljava/lang/Object;)V
@@ -246,7 +280,11 @@
         : aload_1
         : ldc           #_                 // int 4
         : aaload
+        : aload_1
+        : ldc           #_                 // int 5
+        : aaload
         : aload_0
+        : invokeinterface #_,  2           // InterfaceMethod org/codehaus/groovy/runtime/callsite/CallSite.call:(Ljava/lang/Object;)Ljava/lang/Object;
         : ldc           #_                 // String lajos
         : invokeinterface #_,  3           // InterfaceMethod org/codehaus/groovy/runtime/callsite/CallSite.call:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
         : pop
@@ -255,27 +293,31 @@
         line 11: 5
       LocalVariableTable:
         Start  Length  Slot  Name   Signature
-            0      18     0 server   Ljava/lang/Object;
+            0      27     0 server   Ljava/lang/Object;
```
