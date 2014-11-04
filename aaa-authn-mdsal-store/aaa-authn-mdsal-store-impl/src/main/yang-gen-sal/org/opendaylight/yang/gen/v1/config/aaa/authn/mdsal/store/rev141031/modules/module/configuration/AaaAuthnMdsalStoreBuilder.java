package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration;
import org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store.DomBroker;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store.DataBroker;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore} instances.
 * @see org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore
 */
public class AaaAuthnMdsalStoreBuilder {

    private DataBroker _dataBroker;
    private DomBroker _domBroker;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> augmentation = new HashMap<>();

    public AaaAuthnMdsalStoreBuilder() {
    } 
    

    public AaaAuthnMdsalStoreBuilder(AaaAuthnMdsalStore base) {
        this._dataBroker = base.getDataBroker();
        this._domBroker = base.getDomBroker();
        if (base instanceof AaaAuthnMdsalStoreImpl) {
            AaaAuthnMdsalStoreImpl _impl = (AaaAuthnMdsalStoreImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public DataBroker getDataBroker() {
        return _dataBroker;
    }
    
    public DomBroker getDomBroker() {
        return _domBroker;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public AaaAuthnMdsalStoreBuilder setDataBroker(DataBroker value) {
        this._dataBroker = value;
        return this;
    }
    
    public AaaAuthnMdsalStoreBuilder setDomBroker(DomBroker value) {
        this._domBroker = value;
        return this;
    }
    
    public AaaAuthnMdsalStoreBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public AaaAuthnMdsalStore build() {
        return new AaaAuthnMdsalStoreImpl(this);
    }

    private static final class AaaAuthnMdsalStoreImpl implements AaaAuthnMdsalStore {

        public java.lang.Class<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore.class;
        }

        private final DataBroker _dataBroker;
        private final DomBroker _domBroker;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> augmentation = new HashMap<>();

        private AaaAuthnMdsalStoreImpl(AaaAuthnMdsalStoreBuilder base) {
            this._dataBroker = base.getDataBroker();
            this._domBroker = base.getDomBroker();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public DataBroker getDataBroker() {
            return _dataBroker;
        }
        
        @Override
        public DomBroker getDomBroker() {
            return _domBroker;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_dataBroker == null) ? 0 : _dataBroker.hashCode());
            result = prime * result + ((_domBroker == null) ? 0 : _domBroker.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore other = (org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore)obj;
            if (_dataBroker == null) {
                if (other.getDataBroker() != null) {
                    return false;
                }
            } else if(!_dataBroker.equals(other.getDataBroker())) {
                return false;
            }
            if (_domBroker == null) {
                if (other.getDomBroker() != null) {
                    return false;
                }
            } else if(!_domBroker.equals(other.getDomBroker())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AaaAuthnMdsalStoreImpl otherImpl = (AaaAuthnMdsalStoreImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>>, Augmentation<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.AaaAuthnMdsalStore>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("AaaAuthnMdsalStore [");
            boolean first = true;
        
            if (_dataBroker != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_dataBroker=");
                builder.append(_dataBroker);
             }
            if (_domBroker != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_domBroker=");
                builder.append(_domBroker);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
