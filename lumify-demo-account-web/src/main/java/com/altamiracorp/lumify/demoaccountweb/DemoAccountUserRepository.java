package com.altamiracorp.lumify.demoaccountweb;

import com.altamiracorp.bigtable.model.*;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.bigtable.model.user.accumulo.AccumuloUserContext;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUserMetadata;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUserRowKey;
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
public class DemoAccountUserRepository extends Repository<DemoAccountUser> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAccountUserRepository.class.getName());
    private ModelUserContext context;
    @Inject
    public DemoAccountUserRepository(ModelSession modelSession) {
        super(modelSession);
        context = new AccumuloUserContext(new Authorizations());
    }

    public DemoAccountUser getOrCreateUser(String email, boolean shouldRegister) {

        DemoAccountUser user = findByRowKey(email.toLowerCase(), context);
        if (user == null) {
            user = new DemoAccountUser(new DemoAccountUserRowKey(email));
            user.getMetadata().setEmail(email).setOptIn(shouldRegister);
        } else {
            user.getMetadata().setOptIn(shouldRegister);
        }

        return user;
    }

    public void save(DemoAccountUser user) {
        save(user, context);
    }

    public void generateToken(DemoAccountUser user) {
       user.getMetadata().setTokenExpiration(DateUtils.addHours(new Date(), 24));
        user.getMetadata().setToken(Hashing.goodFastHash(64)
                .newHasher()
                .putLong(user.getMetadata().getTokenExpiration().getTime())
                .putString(user.getMetadata().getEmail())
                .hash()
                .toString());
    }

    public DemoAccountUser getUserFromToken(String token) {
        // FIXME: better way to not load all users?
        for (DemoAccountUser user : findAll(context)) {
            String usersToken = user.getMetadata().getToken();
            if (usersToken != null && token.equals(usersToken)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public DemoAccountUser fromRow(Row row) {
        DemoAccountUser user = new DemoAccountUser(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(DemoAccountUserMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                user.addColumnFamily(new DemoAccountUserMetadata().addColumns(columns));
            } else {
                user.addColumnFamily(columnFamily);
            }
        }
        return user;
    }

    @Override
    public Row toRow(DemoAccountUser obj) {
        return obj;
    }

    @Override
    public String getTableName() {
        return DemoAccountUser.TABLE_NAME;
    }
}
