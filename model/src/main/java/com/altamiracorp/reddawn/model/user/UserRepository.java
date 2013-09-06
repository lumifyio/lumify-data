package com.altamiracorp.reddawn.model.user;

import com.altamiracorp.reddawn.model.*;

import java.util.Collection;
import java.util.List;

public class UserRepository extends Repository<User> {
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

    public User findOrAddUser(Session session, String userName) {
        User user = findByUserName(session, userName);
        if (user != null) {
            return user;
        }

        user = new User();
        user.getMetadata().setUserName(userName);
        save(session, user);
        return user;
    }

    private User findByUserName(Session session, String userName) {
        List<User> users = findAll(session);
        for (User user : users) {
            if (userName.equals(user.getMetadata().getUserName())) {
                return user;
            }
        }
        return null;
    }
}
