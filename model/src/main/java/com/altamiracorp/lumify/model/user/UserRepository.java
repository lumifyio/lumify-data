package com.altamiracorp.lumify.model.user;

import java.util.Collection;
import java.util.List;

import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.google.inject.Inject;

public class UserRepository extends Repository<User> {
    @Inject
    public UserRepository(final ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public User fromRow(Row row) {
        User user = new User(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(UserMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                user.addColumnFamily(new UserMetadata().addColumns(columns));
            } else {
                user.addColumnFamily(columnFamily);
            }
        }
        return user;
    }

    @Override
    public Row toRow(User workspace) {
        return workspace;
    }

    @Override
    public String getTableName() {
        return User.TABLE_NAME;
    }

    public User findOrAddUser(String userName, com.altamiracorp.lumify.core.user.User authUser) {
        User user = findByUserName(userName, authUser);
        if (user != null) {
            return user;
        }

        user = new User();
        user.getMetadata().setUserName(userName);
        save(user, authUser);
        return user;
    }

    private User findByUserName(String userName, com.altamiracorp.lumify.core.user.User authUser) {
        List<User> users = findAll(authUser);
        for (User user : users) {
            if (userName.equals(user.getMetadata().getUserName())) {
                return user;
            }
        }
        return null;
    }
}
