package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DomBroker;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.authorization.grp.Policies;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ResourceType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.RoleType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ServiceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DataBroker;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService} instances.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService
 */
public class AaaAuthzServiceBuilder {

    private ActionType _action;
    private java.lang.String _authzRole;
    private DataBroker _dataBroker;
    private DomBroker _domBroker;
    private List<Policies> _policies;
    private ResourceType _resource;
    private RoleType _role;
    private ServiceType _service;

    private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> augmentation = new HashMap<>();

    public AaaAuthzServiceBuilder() {
    } 
    
    public AaaAuthzServiceBuilder(org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp arg) {
        this._policies = arg.getPolicies();
    }
    

    public AaaAuthzServiceBuilder(AaaAuthzService base) {
        this._action = base.getAction();
        this._authzRole = base.getAuthzRole();
        this._dataBroker = base.getDataBroker();
        this._domBroker = base.getDomBroker();
        this._policies = base.getPolicies();
        this._resource = base.getResource();
        this._role = base.getRole();
        this._service = base.getService();
        if (base instanceof AaaAuthzServiceImpl) {
            AaaAuthzServiceImpl _impl = (AaaAuthzServiceImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp) {
            this._policies = ((org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp)arg).getPolicies();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp] \n" +
              "but was: " + arg
            );
        }
    }

    public ActionType getAction() {
        return _action;
    }
    
    public java.lang.String getAuthzRole() {
        return _authzRole;
    }
    
    public DataBroker getDataBroker() {
        return _dataBroker;
    }
    
    public DomBroker getDomBroker() {
        return _domBroker;
    }
    
    public List<Policies> getPolicies() {
        return _policies;
    }
    
    public ResourceType getResource() {
        return _resource;
    }
    
    public RoleType getRole() {
        return _role;
    }
    
    public ServiceType getService() {
        return _service;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public AaaAuthzServiceBuilder setAction(ActionType value) {
        this._action = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setAuthzRole(java.lang.String value) {
        this._authzRole = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setDataBroker(DataBroker value) {
        this._dataBroker = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setDomBroker(DomBroker value) {
        this._domBroker = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setPolicies(List<Policies> value) {
        this._policies = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setResource(ResourceType value) {
        this._resource = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setRole(RoleType value) {
        this._role = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder setService(ServiceType value) {
        this._service = value;
        return this;
    }
    
    public AaaAuthzServiceBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public AaaAuthzService build() {
        return new AaaAuthzServiceImpl(this);
    }

    private static final class AaaAuthzServiceImpl implements AaaAuthzService {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService.class;
        }

        private final ActionType _action;
        private final java.lang.String _authzRole;
        private final DataBroker _dataBroker;
        private final DomBroker _domBroker;
        private final List<Policies> _policies;
        private final ResourceType _resource;
        private final RoleType _role;
        private final ServiceType _service;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> augmentation = new HashMap<>();

        private AaaAuthzServiceImpl(AaaAuthzServiceBuilder base) {
            this._action = base.getAction();
            this._authzRole = base.getAuthzRole();
            this._dataBroker = base.getDataBroker();
            this._domBroker = base.getDomBroker();
            this._policies = base.getPolicies();
            this._resource = base.getResource();
            this._role = base.getRole();
            this._service = base.getService();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public ActionType getAction() {
            return _action;
        }
        
        @Override
        public java.lang.String getAuthzRole() {
            return _authzRole;
        }
        
        @Override
        public DataBroker getDataBroker() {
            return _dataBroker;
        }
        
        @Override
        public DomBroker getDomBroker() {
            return _domBroker;
        }
        
        @Override
        public List<Policies> getPolicies() {
            return _policies;
        }
        
        @Override
        public ResourceType getResource() {
            return _resource;
        }
        
        @Override
        public RoleType getRole() {
            return _role;
        }
        
        @Override
        public ServiceType getService() {
            return _service;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_action == null) ? 0 : _action.hashCode());
            result = prime * result + ((_authzRole == null) ? 0 : _authzRole.hashCode());
            result = prime * result + ((_dataBroker == null) ? 0 : _dataBroker.hashCode());
            result = prime * result + ((_domBroker == null) ? 0 : _domBroker.hashCode());
            result = prime * result + ((_policies == null) ? 0 : _policies.hashCode());
            result = prime * result + ((_resource == null) ? 0 : _resource.hashCode());
            result = prime * result + ((_role == null) ? 0 : _role.hashCode());
            result = prime * result + ((_service == null) ? 0 : _service.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService other = (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService)obj;
            if (_action == null) {
                if (other.getAction() != null) {
                    return false;
                }
            } else if(!_action.equals(other.getAction())) {
                return false;
            }
            if (_authzRole == null) {
                if (other.getAuthzRole() != null) {
                    return false;
                }
            } else if(!_authzRole.equals(other.getAuthzRole())) {
                return false;
            }
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
            if (_policies == null) {
                if (other.getPolicies() != null) {
                    return false;
                }
            } else if(!_policies.equals(other.getPolicies())) {
                return false;
            }
            if (_resource == null) {
                if (other.getResource() != null) {
                    return false;
                }
            } else if(!_resource.equals(other.getResource())) {
                return false;
            }
            if (_role == null) {
                if (other.getRole() != null) {
                    return false;
                }
            } else if(!_role.equals(other.getRole())) {
                return false;
            }
            if (_service == null) {
                if (other.getService() != null) {
                    return false;
                }
            } else if(!_service.equals(other.getService())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AaaAuthzServiceImpl otherImpl = (AaaAuthzServiceImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("AaaAuthzService [");
            boolean first = true;
        
            if (_action != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_action=");
                builder.append(_action);
             }
            if (_authzRole != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_authzRole=");
                builder.append(_authzRole);
             }
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
            if (_policies != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_policies=");
                builder.append(_policies);
             }
            if (_resource != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_resource=");
                builder.append(_resource);
             }
            if (_role != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_role=");
                builder.append(_role);
             }
            if (_service != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_service=");
                builder.append(_service);
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
