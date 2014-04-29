package io.lumify.sql.model.user;

import io.lumify.core.exception.LumifyException;
import io.lumify.core.model.user.AuthorizationRepository;
import io.lumify.core.model.user.UserPasswordUtil;
import io.lumify.core.model.user.UserStatus;
import io.lumify.core.user.Privilege;
import io.lumify.core.user.User;
import io.lumify.sql.model.workspace.SqlWorkspace;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.securegraph.util.IterableUtils;

import java.util.EnumSet;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SqlUserRepositoryTest {
    private final String HIBERNATE_IN_MEM_CFG_XML = "hibernateInMem.cfg.xml";
    private SqlUserRepository sqlUserRepository;
    private static org.hibernate.cfg.Configuration configuration;
    private static SessionFactory sessionFactory;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Before
    public void setup() {
        configuration = new org.hibernate.cfg.Configuration();
        configuration.configure(HIBERNATE_IN_MEM_CFG_XML);
        ServiceRegistry serviceRegistryBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistryBuilder);
        sqlUserRepository = new SqlUserRepository(authorizationRepository, sessionFactory);
    }

    @Test
    public void testAddUser() throws Exception {
        SqlUser sqlUser1 = (SqlUser) sqlUserRepository.addUser("abc", "test user1", "", Privilege.ALL, new String[0]);
        assertEquals("abc", sqlUser1.getUsername());
        assertEquals("test user1", sqlUser1.getDisplayName());
        assertEquals("OFFLINE", sqlUser1.getUserStatus());
        assertTrue(sqlUser1.getPrivileges().contains(Privilege.READ));
        assertTrue(sqlUser1.getPrivileges().contains(Privilege.EDIT));
        assertTrue(sqlUser1.getPrivileges().contains(Privilege.PUBLISH));
        assertTrue(sqlUser1.getPrivileges().contains(Privilege.ADMIN));

        SqlUser sqlUser2 = (SqlUser) sqlUserRepository.addUser("def", "test user2", null, Privilege.NONE, new String[0]);
        assertNull(sqlUser2.getPasswordHash());
        assertNull(sqlUser2.getPasswordSalt());
        assertEquals("def", sqlUser2.getUsername());
        assertEquals("test user2", sqlUser2.getDisplayName());
        assertEquals(2, sqlUser2.getId());
        assertEquals("OFFLINE", sqlUser2.getUserStatus());
        assertFalse(sqlUser2.getPrivileges().contains(Privilege.READ));
        assertFalse(sqlUser2.getPrivileges().contains(Privilege.EDIT));
        assertFalse(sqlUser2.getPrivileges().contains(Privilege.PUBLISH));
        assertFalse(sqlUser2.getPrivileges().contains(Privilege.ADMIN));

        SqlUser sqlUser3 = (SqlUser) sqlUserRepository.addUser("ghi", "test user3", "&gdja81", EnumSet.of(Privilege.READ), new String[0]);
        byte[] salt = sqlUser3.getPasswordSalt();
        byte[] passwordHash = UserPasswordUtil.hashPassword("&gdja81", salt);
        assertTrue(UserPasswordUtil.validatePassword("&gdja81", salt, passwordHash));
        assertEquals("ghi", sqlUser3.getUsername());
        assertEquals("test user3", sqlUser3.getDisplayName());
        assertEquals(3, sqlUser3.getId());
        assertEquals("OFFLINE", sqlUser3.getUserStatus());
        assertTrue(sqlUser3.getPrivileges().contains(Privilege.READ));
        assertFalse(sqlUser3.getPrivileges().contains(Privilege.EDIT));
        assertFalse(sqlUser3.getPrivileges().contains(Privilege.PUBLISH));
        assertFalse(sqlUser3.getPrivileges().contains(Privilege.ADMIN));
    }

    @Test(expected = LumifyException.class)
    public void testAddUserWithExisitingUsername() {
        sqlUserRepository.addUser("123", "test user1", "&gdja81", Privilege.ALL, new String[0]);
        sqlUserRepository.addUser("123", "test user1", null, Privilege.ALL, new String[0]);
    }

    @Test
    public void testFindById() throws Exception {
        sqlUserRepository.addUser("12345", "test user", "&gdja81", Privilege.ALL, new String[0]);
        SqlUser user = (SqlUser) sqlUserRepository.findById("1");
        byte[] salt = user.getPasswordSalt();
        byte[] passwordHash = UserPasswordUtil.hashPassword("&gdja81", salt);
        assertTrue(UserPasswordUtil.validatePassword("&gdja81", salt, passwordHash));
        assertEquals("12345", user.getUsername());
        assertEquals("test user", user.getDisplayName());
        assertEquals(1, user.getId());
        assertEquals("OFFLINE", user.getUserStatus());

        assertNull(sqlUserRepository.findById("2"));
    }


    @Test
    public void testFindAll() throws Exception {
        Iterable<User> userIterable = sqlUserRepository.findAll();
        assertTrue(IterableUtils.count(userIterable) == 0);

        sqlUserRepository.addUser("123", "test user1", "&gdja81", Privilege.ALL, new String[0]);
        sqlUserRepository.addUser("456", "test user2", null, Privilege.ALL, new String[0]);
        sqlUserRepository.addUser("789", "test user3", null, Privilege.ALL, new String[0]);
        userIterable = sqlUserRepository.findAll();
        assertTrue(IterableUtils.count(userIterable) == 3);
    }

    @Test
    public void testSetPassword() throws Exception {
        SqlUser testUser = (SqlUser) sqlUserRepository.addUser("abcd", "test user", "1234", Privilege.ALL, new String[0]);

        assertTrue(sqlUserRepository.findByUsername("abcd") != null);
        assertTrue(UserPasswordUtil.validatePassword("1234", testUser.getPasswordSalt(), testUser.getPasswordHash()));

        sqlUserRepository.setPassword(testUser, "ijk");
        assertTrue(UserPasswordUtil.validatePassword("ijk", testUser.getPasswordSalt(), testUser.getPasswordHash()));
    }

    @Test(expected = LumifyException.class)
    public void testSetPasswordWithNullUser() {
        sqlUserRepository.setPassword(null, "1u201");
    }

    @Test(expected = NullPointerException.class)
    public void testSetPasswordWithNullPassword() {
        sqlUserRepository.setPassword(new SqlUser(), null);
    }

    @Test(expected = LumifyException.class)
    public void testSetPasswordWithNoUserId() {
        sqlUserRepository.setPassword(new SqlUser(), "123");
    }

    @Test(expected = LumifyException.class)
    public void testSetPasswordWithNonExistingUser() {
        SqlUser sqlUser = new SqlUser();
        sqlUser.setId(1);
        sqlUserRepository.setPassword(sqlUser, "123");
    }

    @Test
    public void testIsPasswordValid() throws Exception {
        SqlUser testUser = (SqlUser) sqlUserRepository.addUser("1234", "test user", null, Privilege.ALL, new String[0]);
        assertFalse(sqlUserRepository.isPasswordValid(testUser, ""));

        sqlUserRepository.setPassword(testUser, "abc");
        assertTrue(sqlUserRepository.isPasswordValid(testUser, "abc"));
    }

    @Test(expected = LumifyException.class)
    public void testIsPasswordValidWithNullUser() {
        sqlUserRepository.isPasswordValid(null, "1234");
    }

    @Test(expected = NullPointerException.class)
    public void testIsPasswordValidWithNullPassword() {
        sqlUserRepository.isPasswordValid(new SqlUser(), null);
    }

    @Test(expected = LumifyException.class)
    public void testIsPasswordValidWithNonExisitingUser() {
        sqlUserRepository.isPasswordValid(new SqlUser(), "123");
    }

    @Test
    public void testSetCurrentWorkspace() throws Exception {
        SqlWorkspace sqlWorkspace = new SqlWorkspace();
        sqlWorkspace.setDisplayTitle("workspace1");
        sqlUserRepository.addUser("123", "abc", null, Privilege.ALL, new String[0]);
        sqlUserRepository.setCurrentWorkspace("1", sqlWorkspace);
        SqlUser testUser = (SqlUser) sqlUserRepository.findById("1");
        assertEquals("workspace1", testUser.getCurrentWorkspace().getDisplayTitle());
    }

    @Test(expected = LumifyException.class)
    public void testSetCurrentWorkspaceWithNonExisitingUser() {
        sqlUserRepository.setCurrentWorkspace("1", new SqlWorkspace());
    }

    @Test
    public void testSetStatus() throws Exception {
        sqlUserRepository.addUser("123", "abc", null, Privilege.ALL, new String[0]);
        sqlUserRepository.setStatus("1", UserStatus.ONLINE);

        SqlUser testUser = (SqlUser) sqlUserRepository.findById("1");
        assertEquals(UserStatus.ONLINE.name(), testUser.getUserStatus());
    }

    @Test(expected = LumifyException.class)
    public void testSetStatusWithNonExisitingUser() {
        sqlUserRepository.setStatus("1", UserStatus.OFFLINE);
    }
}
