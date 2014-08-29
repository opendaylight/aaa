package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.ServiceRef;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.Module;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>aaa-authz-service-impl</b>
 * <br />(Source path: <i>META-INF/yang/aaa-authz-service-impl.yang</i>):
 * <pre>
 * container dom-broker {
 *     leaf type {
 *         type service-type-ref;
 *     }
 *     leaf name {
 *         type leafref;
 *     }
 *     uses service-ref {
 *         refine (urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv?revision=2014-07-01)type {
 *             leaf type {
 *                 type service-type-ref;
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>aaa-authz-service-impl/modules/module/configuration/(urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv?revision=2014-07-01)aaa-authz-service/dom-broker</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DomBrokerBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DomBrokerBuilder
 */
public interface DomBroker
    extends
    ChildOf<Module>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.aaa.authz.srv.rev140701.modules.module.configuration.aaa.authz.service.DomBroker>,
    ServiceRef
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:controller:config:aaa-authz:srv","2014-07-01","dom-broker");;


}

