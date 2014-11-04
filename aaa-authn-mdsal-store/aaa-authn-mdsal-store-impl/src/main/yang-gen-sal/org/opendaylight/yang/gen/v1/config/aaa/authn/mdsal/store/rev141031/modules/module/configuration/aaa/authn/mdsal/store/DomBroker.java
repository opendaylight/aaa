package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.ServiceRef;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.Module;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>aaa-authn-mdsal-store-cfg</b>
 * <br />(Source path: <i>META-INF/yang/aaa-authn-mdsal-store-cfg.yang</i>):
 * <pre>
 * container dom-broker {
 *     leaf type {
 *         type leafref;
 *     }
 *     leaf name {
 *         type leafref;
 *     }
 *     uses service-ref {
 *         refine (config:aaa:authn:mdsal:store?revision=2014-10-31)type {
 *             leaf type {
 *                 type leafref;
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>aaa-authn-mdsal-store-cfg/modules/module/configuration/(config:aaa:authn:mdsal:store?revision=2014-10-31)aaa-authn-mdsal-store/dom-broker</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store.DomBrokerBuilder}.
 * @see org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store.DomBrokerBuilder
 */
public interface DomBroker
    extends
    ChildOf<Module>,
    Augmentable<org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.modules.module.configuration.aaa.authn.mdsal.store.DomBroker>,
    ServiceRef
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("config:aaa:authn:mdsal:store","2014-10-31","dom-broker");;


}

