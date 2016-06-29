/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.jpa;

import com.google.common.reflect.Reflection;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;

import javax.persistence.EntityManagerFactory;

import static org.eclipse.che.api.user.server.jpa.H2DBServerListener.ENTITY_MANAGER_FACTORY_ATTR_NAME;

/**
 * @author Yevhenii Voevodin
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        final EntityManagerFactory factoryProxy = Reflection.newProxy(EntityManagerFactory.class, (proxy, method, args) -> {
            if (method.getName().startsWith("createEntityManager")) {
                final EntityManagerFactory factory = (EntityManagerFactory)getTestContext().getAttribute(ENTITY_MANAGER_FACTORY_ATTR_NAME);
                return factory.createEntityManager();
            }
            return null;
        });
        bind(EntityManagerFactory.class).toInstance(factoryProxy);

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserJpaTckRepository.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).to(ProfileJpaTckRepository.class);

        bind(UserDao.class).to(JpaUserDao.class);
        bind(ProfileDao.class).to(JpaProfileDao.class);
        // SHA-512 ecnryptor is faster than PBKDF2 so it is better for testing
        bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class).in(Singleton.class);
    }
}
