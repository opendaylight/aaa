package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DomBroker;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ResourceType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.RoleType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ServiceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.module.Configuration;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationGrp;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>aaa-authz-service-impl</b>
 * <br />(Source path: <i>META-INF/yang/aaa-authz-service-impl.yang</i>):
 * <pre>
 * case aaa-authz-service {
 *     container dom-broker {
 *         leaf type {
 *             type service-type-ref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv?revision=2014-07-01)type {
 *                 leaf type {
 *                     type service-type-ref;
 *                 }
 *             }
 *         }
 *     }
 *     container data-broker {
 *         leaf type {
 *             type service-type-ref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv?revision=2014-07-01)type {
 *                 leaf type {
 *                     type service-type-ref;
 *                 }
 *             }
 *         }
 *     }
 *     leaf authz-role {
 *         type string;
 *     }
 *     leaf service {
 *         type service-type;
 *     }
 *     leaf action {
 *         type action-type;
 *     }
 *     leaf resource {
 *         type resource-type;
 *     }
 *     leaf role {
 *         type role-type;
 *     }
 *     list policies {
 *         key "service"
 *         leaf service {
 *             type service-type;
 *         }
 *         leaf action {
 *             type action-type;
 *         }
 *         leaf resource {
 *             type resource-type;
 *         }
 *         leaf role {
 *             type role-type;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>aaa-authz-service-impl/modules/module/configuration/(urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv?revision=2014-07-01)aaa-authz-service</i>
 */
public interface AaaAuthzService
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.AaaAuthzService>,
    AuthorizationGrp,
    Configuration
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv","2014-07-01","aaa-authz-service");;

    DomBroker getDomBroker();
    
    DataBroker getDataBroker();
    
    java.lang.String getAuthzRole();
    
    ServiceType getService();
    
    ActionType getAction();
    
    ResourceType getResource();
    
    RoleType getRole();

}

