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
package org.eclipse.che.api.user.server.spi.tck;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link UserDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = UserDaoTest.SUITE_NAME)
public class UserDaoTest {

    public static final String SUITE_NAME = "UserDaoTck";

    private static final int COUNT_OF_USERS = 5;

    private UserImpl[] users;

    @Inject
    private UserDao userDao;

    @Inject
    private TckRepository<UserImpl> tckRepository;

    @BeforeMethod
    public void setUp() throws TckRepositoryException {
        users = new UserImpl[COUNT_OF_USERS];

        for (int i = 0; i < users.length; i++) {
            final String id = NameGenerator.generate("user", Constants.ID_LENGTH);
            final String name = "user_name-" + i;
            final String email = name + "@eclipse.org";
            final String password = NameGenerator.generate("", Constants.PASSWORD_LENGTH);
            final List<String> aliases = new ArrayList<>(asList("google:" + name, "github:" + name));
            users[i] = new UserImpl(id, email, name, password, aliases);
        }

        tckRepository.createAll(Arrays.asList(users));
    }

    @AfterMethod
    public void cleanUp() throws TckRepositoryException {
        tckRepository.removeAll();
    }

    @Test
    public void shouldGetUserByNameAndPassword() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByAliasAndPassword(user.getName(), user.getPassword()), user);
    }

    @Test
    public void shouldGetUserByEmailAndPassword() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByAliasAndPassword(user.getEmail(), user.getPassword()), user);
    }

    @Test
    public void shouldGetUserByAliasAndPassword() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByAliasAndPassword(user.getAliases().get(0), user.getPassword()), user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfUserWithSuchAliasDoesNotExist() throws Exception {
        final UserImpl user = users[0];

        userDao.getByAliasAndPassword(user.getId(), user.getPassword());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingUserByAliasAndPasswordAndPasswordIsWrong() throws Exception {
        final UserImpl user = users[0];

        userDao.getByAliasAndPassword(user.getName(), "fake" + user.getPassword());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenAuthorizingUserWithNullAlias() throws Exception {
        userDao.getByAliasAndPassword(null, "password");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenAuthorizingUserWithNullPassword() throws Exception {
        userDao.getByAliasAndPassword("alias", null);
    }

    @Test
    public void shouldGetUserById() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getById(user.getId()), user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingUserById() throws Exception {
        userDao.getById("non-existing");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingUserByNullId() throws Exception {
        userDao.getById(null);
    }

    @Test
    public void shouldGetUserByEmail() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByEmail(user.getEmail()), user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingUserByEmail() throws Exception {
        userDao.getByEmail("non-existing-email@eclipse.org");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingUserByNullEmail() throws Exception {
        userDao.getByEmail(null);
    }

    @Test
    public void shouldGetUserByName() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByName(user.getName()), user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNoFountExceptionWhenGettingNonExistingUserByName() throws Exception {
        userDao.getByName("non-existing-name");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingUserByNullName() throws Exception {
        userDao.getByName(null);
    }

    @Test
    public void shouldGetUserByAlias() throws Exception {
        final UserImpl user = users[0];

        assertEqualsNoPassword(userDao.getByAlias(user.getAliases().get(0)), user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingUserByAlias() throws Exception {
        userDao.getByAlias("non-existing-alias");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingUserByNullAlias() throws Exception {
        userDao.getByAlias(null);
    }

    @Test(dependsOnMethods = "shouldGetUserById")
    public void shouldCreateUser() throws Exception {
        final UserImpl newUser = new UserImpl("user123",
                                              "user123@eclipse.org",
                                              "user_name",
                                              "password",
                                              asList("google:user123", "github:user123"));

        userDao.create(newUser);

        assertEqualsNoPassword(userDao.getById(newUser.getId()), newUser);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingUserWithExistingId() throws Exception {
        final UserImpl newUser = new UserImpl(users[0].getId(),
                                              "user123@eclipse.org",
                                              "user_name",
                                              "password",
                                              asList("google:user123", "github:user123"));

        userDao.create(newUser);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingUserWithExistingEmail() throws Exception {
        final UserImpl newUser = new UserImpl("user123",
                                              users[0].getEmail(),
                                              "user_name",
                                              "password",
                                              asList("google:user123", "github:user123"));

        userDao.create(newUser);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingUserWithExistingName() throws Exception {
        final UserImpl newUser = new UserImpl("user123",
                                              "user123@eclipse.org",
                                              users[0].getName(),
                                              "password",
                                              asList("google:user123", "github:user123"));

        userDao.create(newUser);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingUserWithExistingAliases() throws Exception {
        final UserImpl newUser = new UserImpl("user123",
                                              "user123@eclipse.org",
                                              "user_name",
                                              "password",
                                              users[0].getAliases());

        userDao.create(newUser);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrownNpeWhenTryingToCreateNullUser() throws Exception {
        userDao.create(null);
    }

    @Test(dependsOnMethods = "shouldGetUserById")
    public void shouldUpdateUser() throws Exception {
        final UserImpl user = users[0];

        userDao.update(new UserImpl(user.getId(),
                                    "new-email",
                                    "new-name",
                                    null,
                                    asList("google:new-alias", "github:new-alias")));

        final UserImpl updated = userDao.getById(user.getId());
        assertEquals(updated.getId(), user.getId());
        assertEquals(updated.getEmail(), "new-email");
        assertEquals(updated.getName(), "new-name");
        assertEquals(new HashSet<>(updated.getAliases()), new HashSet<>(asList("google:new-alias", "github:new-alias")));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenUpdatingUserWithReservedEmail() throws Exception {
        final UserImpl user = users[0];

        user.setEmail(users[1].getEmail());

        userDao.update(user);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenUpdatingUserWithReservedName() throws Exception {
        final UserImpl user = users[0];

        user.setName(users[1].getName());

        userDao.update(user);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenUpdatingUserWithReservedAlias() throws Exception {
        final UserImpl user = users[0];

        user.setAliases(users[1].getAliases());

        userDao.update(user);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingUser() throws Exception {
        userDao.update(new UserImpl("non-existing-id",
                                    "new-email",
                                    "new-name",
                                    "new-password",
                                    asList("google:new-alias", "github:new-alias")));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingNull() throws Exception {
        userDao.update(null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingUserById")
    public void shouldRemoveUser() throws Exception {
        final UserImpl user = users[0];

        userDao.remove(user.getId());
        userDao.getById(user.getId());
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenRemovingNonExistingUser() throws Exception {
        userDao.remove("non-existing-user");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        userDao.remove(null);
    }

    private static void assertEqualsNoPassword(User actual, User expected) {
        assertNotNull(actual, "Expected not-null user");
        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getEmail(), expected.getEmail());
        assertEquals(actual.getName(), expected.getName());
        assertEquals(new HashSet<>(actual.getAliases()), new HashSet<>(expected.getAliases()));
    }
}
