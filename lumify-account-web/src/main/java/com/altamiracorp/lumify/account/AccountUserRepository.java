package com.altamiracorp.lumify.account;

import com.altamiracorp.bigtable.model.*;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.bigtable.model.user.accumulo.AccumuloUserContext;
import com.altamiracorp.lumify.account.model.AccountUser;
import com.altamiracorp.lumify.account.model.AccountUserData;
import com.altamiracorp.lumify.account.model.AccountUserRowKey;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;

@Singleton
public class AccountUserRepository extends Repository<AccountUser> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountUserRepository.class.getName());
    private ModelUserContext context;

    @Inject
    public AccountUserRepository(ModelSession modelSession) {
        super(modelSession);
        context = new AccumuloUserContext(new Authorizations());
    }

    public AccountUser getOrCreateUser(String email, boolean shouldRegister) {

        AccountUser user = findByRowKey(email.toLowerCase(), context);
        if (user == null) {
            user = new AccountUser(new AccountUserRowKey(email));
            user.getData().setEmail(email).setOptIn(shouldRegister).setReset(false);
        } else {
            user.getData().setOptIn(shouldRegister).setReset(true);
        }

        return user;
    }

    public void save(AccountUser user) {
        save(user);
    }

    public void generateToken(AccountUser user) {
        user.getData().setTokenExpiration(DateUtils.addHours(new Date(), 24));
        user.getData().setToken(Hashing.goodFastHash(64)
                .newHasher()
                .putLong(user.getData().getTokenExpiration().getTime())
                .putString(user.getData().getEmail())
                .hash()
                .toString());
    }

    public AccountUser getUserFromToken(String token) {
        // FIXME: better way to not load all users?
        // "maybe org.apache.accumulo.core.iterators.user.RowFilter" -Joe
        for (AccountUser user : findAll(context)) {
            String usersToken = user.getData().getToken();
            if (usersToken != null && token.equals(usersToken)) {
                Date tokenExpiration = user.getData().getTokenExpiration();
                if (tokenExpiration != null && tokenExpiration.after(new Date())) {
                    return user;
                } else {
                    LOGGER.debug("found expired token");
                    return null;
                }
            }
        }

        LOGGER.debug("token not found");
        return null;
    }

    @Override
    public AccountUser fromRow(Row row) {
        AccountUser user = new AccountUser(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(AccountUserData.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                user.addColumnFamily(new AccountUserData().addColumns(columns));
            } else {
                user.addColumnFamily(columnFamily);
            }
        }
        return user;
    }

    @Override
    public Row toRow(AccountUser obj) {
        return obj;
    }

    @Override
    public String getTableName() {
        return AccountUser.TABLE_NAME;
    }
}
