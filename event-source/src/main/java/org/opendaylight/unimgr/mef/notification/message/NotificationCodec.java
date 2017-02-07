package org.opendaylight.unimgr.mef.notification.message;

import javassist.ClassPool;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.messagebus.eventaggregator.rev141202.TopicNotification;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that create BI from BA object.
 */
public class NotificationCodec {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationCodec.class);
    private static final YangInstanceIdentifier.NodeIdentifier CLASS_NAME_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "class-name").intern());
    private static final YangInstanceIdentifier.NodeIdentifier YANG_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "yang-name").intern());
    private static final YangInstanceIdentifier.NodeIdentifier PAYLOAD_ARG = new YangInstanceIdentifier.NodeIdentifier(QName.create(TopicNotification.QNAME, "payload"));
    private static NotificationCodec notificationCodec = null;
    private BindingNormalizedNodeCodecRegistry bindingNormalizedNodeCodecRegistry;
    private Set<Class<?>> moduleClasses;
    private Set<YangModuleInfo> moduleInfos;
    private ClassPool classPool;


    private NotificationCodec(){
        initBindingNormalizedNodeCodecRegistry();
    }

    public static NotificationCodec getInstance(){
        if(notificationCodec==null){
            synchronized (NotificationCodec.class){
                if(notificationCodec==null){
                    notificationCodec = new NotificationCodec();
                }
            }
        }
        return  notificationCodec;
    }

    private void initBindingNormalizedNodeCodecRegistry(){
        moduleClasses = new HashSet<>();
        moduleInfos = new HashSet<>();
        classPool = ClassPool.getDefault();
        JavassistUtils utils =
                JavassistUtils.forClassPool(classPool);
        bindingNormalizedNodeCodecRegistry = new
                BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
    }

    /**
     * @param dataContainer BA object
     * @param instanceIdentifier Instance Identifier of BA object.
     * @return BI object and its YangInstanceIdentifier
     */
    public synchronized Map.Entry<YangInstanceIdentifier,NormalizedNode<?, ?>> toNormalizedNode(DataContainer dataContainer, InstanceIdentifier instanceIdentifier){
        Class<?> cls = dataContainer.getClass();
        updateCodec(cls);

        InstanceIdentifier<DataObject> ii = instanceIdentifier;
        DataObject dataObject = (DataObject) dataContainer;
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = bindingNormalizedNodeCodecRegistry.toNormalizedNode(ii,dataObject);

        return entry;
    }

    public DataContainer fromNotification(ContainerNode body){
        if(body.getChild(CLASS_NAME_ARG).isPresent() && body.getChild(YANG_ARG).isPresent()){
            String className = (String) body.getChild(CLASS_NAME_ARG).get().getValue();
            Class expectedClass;
            try {
                expectedClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOG.warn("ClassNotFoundException, {}",e.toString());
                return null;
            }
            YangInstanceIdentifier yangInstanceIdentifier = (YangInstanceIdentifier) body.getChild(YANG_ARG).get().getValue();
            NormalizedNode<?, ?> normalizedNode = (NormalizedNode<?, ?>) body.getChild(PAYLOAD_ARG).get().getValue();

            DataContainer dataContainer = notificationCodec.fromDataContainerChild(normalizedNode,expectedClass,yangInstanceIdentifier);
            return dataContainer;
        }
        LOG.warn("Class name or YangInstanceIdetifier is not present in notification");
        return null;
    }

    public DataContainer fromDataContainerChild(NormalizedNode<?, ?> data, Class<?> clazz,YangInstanceIdentifier yangInstanceIdentifier){
        updateCodec(clazz);
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = bindingNormalizedNodeCodecRegistry.fromNormalizedNode(yangInstanceIdentifier,data);
        DataContainer dataContainer = entry.getValue();
        return dataContainer;
    }

    private void updateCodec(Class<?> cls){
        if(!moduleClasses.contains(cls)){
            moduleClasses.add(cls);
            addModuleInfo(cls);
        }
    }

    private void addModuleInfo(Class<?> cls){
        YangModuleInfo moduleInfo = getModuleInfo(cls);
        if(!moduleInfos.contains(moduleInfo)){
            final ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
            moduleInfos.add(moduleInfo);
            moduleContext.addModuleInfos(moduleInfos);
            SchemaContext context =  moduleContext.tryToCreateSchemaContext().get();
            BindingRuntimeContext bindingContext = BindingRuntimeContext.create(moduleContext, context);

            bindingNormalizedNodeCodecRegistry.onBindingRuntimeContextUpdated(bindingContext);
        }
    }

    private YangModuleInfo getModuleInfo(Class<?> cls){
        YangModuleInfo moduleInfo = null;
        try {
            moduleInfo = BindingReflections.getModuleInfo(cls);
        } catch (Exception e) {
            LOG.warn("Cannot find module info");
        }
        return moduleInfo;
    }

}
