package org.gradle.demo.api.evolution;

import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;

public class Instrumented {
    // Called by generated code
    @SuppressWarnings("unused")
    public static void groovyCallSites(CallSiteArray array) {
        for (CallSite callSite : array.array) {
            switch (callSite.getName()) {
                case "setTestProperty":
                    array.array[callSite.getIndex()] = new SetTestPropertiesCallSite(callSite);
                    break;
                case "getTestProperty":
                    array.array[callSite.getIndex()] = new GetTestPropertiesCallSite(callSite);
                    break;
            }
        }
    }

    private static class SetTestPropertiesCallSite extends AbstractCallSite {
        public SetTestPropertiesCallSite(CallSite callSite) {
            super(callSite);
        }

        @Override
        public Object call(Object receiver, Object arg1) throws Throwable {
            if (receiver instanceof Server && arg1 instanceof String) {
                ((Server) receiver).getTestProperty().set((String) arg1);
                return null;
            }
            return super.call(receiver, arg1);
        }
    }

    private static class GetTestPropertiesCallSite extends AbstractCallSite {
        public GetTestPropertiesCallSite(CallSite callSite) {
            super(callSite);
        }

        @Override
        public Object call(Object receiver) throws Throwable {
            if (receiver instanceof Server) {
                return ((Server) receiver).getTestProperty().get();
            }
            return super.call(receiver);
        }
    }
}
