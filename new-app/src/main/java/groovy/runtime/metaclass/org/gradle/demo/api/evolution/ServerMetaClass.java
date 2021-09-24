package groovy.runtime.metaclass.org.gradle.demo.api.evolution;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import org.gradle.demo.api.evolution.Server;

// Loaded by groovy runtime automatically
@SuppressWarnings("unused")
public class ServerMetaClass extends DelegatingMetaClass {
    public ServerMetaClass(MetaClass delegate) {
        super(delegate);
    }

    public ServerMetaClass(Class theClass) {
        super(theClass);
    }

    @Override
    public void setProperty(Object object, String property, Object newValue) {
        Server server = (Server) object;
        if ("testProperty".equals(property)) {
            server.getTestProperty().set((String) newValue);
            return;
        }
        super.setProperty(object, property, newValue);
    }
}
