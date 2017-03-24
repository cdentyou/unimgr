package org.opendaylight.unimgr.mef.notification.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.*;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 16.03.17.
 */
public class NotificationCodec {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationCodec.class);
    private static NotificationCodec notificationCodec = null;
    private BindingNormalizedNodeCodecRegistry bindingNormalizedNodeCodecRegistry;
    private Set<Class<?>> moduleClasses;
    private Set<YangModuleInfo> moduleInfos;
    private ClassPool classPool;

    private NotificationCodec(){
        initBindingNormalizedNodeCodecRegistry();
    }

    public static NotificationCodec getInstance(){
        if(notificationCodec == null){
            synchronized (NotificationCodec.class){
                if(notificationCodec == null){
                    notificationCodec = new NotificationCodec();
                }
            }
        }
        return notificationCodec;
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

    public ContainerNode toContainerNode(Notification notification){
        updateCodec(notification.getClass());
        ContainerNode containerNode = bindingNormalizedNodeCodecRegistry.toNormalizedNodeNotification(notification);
        return containerNode;
    }

    /**
     *
     * @param dataContainer BA object
     * @param instanceIdentifier Instance Identifier of BA object.
     * @return BI object
     */
    public Map.Entry<YangInstanceIdentifier,  NormalizedNode<?, ?>> toDataContainerChild(DataContainer dataContainer, InstanceIdentifier instanceIdentifier){
        Class<?> cls = dataContainer.getClass();
        updateCodec(cls);

        InstanceIdentifier<DataObject> ii = instanceIdentifier;
        DataObject dataObject = (DataObject) dataContainer;

        try {
            CtClass ctClass = classPool.get(cls.getName());
            if(ctClass.isFrozen()){
                ctClass.defrost();
            }
        } catch (NotFoundException e) {
            LOG.warn("Could not find class {}",cls.getName());
        }

        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = bindingNormalizedNodeCodecRegistry.toNormalizedNode(ii,dataObject);
        return entry;
    }

    public DataContainer fromDataContainerChild(NormalizedNode<?, ?> data,Class<?> expectedClass,YangInstanceIdentifier yangInstanceIdentifier){
        updateCodec(expectedClass);
        Map.Entry<InstanceIdentifier<?>, DataObject> entry = bindingNormalizedNodeCodecRegistry.fromNormalizedNode(yangInstanceIdentifier,data);
        if(entry == null){
            return null;
        }
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
