package DAO;

import Model.Account;
import Util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AccountDAO {
  


    /**
     * Creates a new user account and inserts it into the database.
     *
     * @param account The Account object representing the new user account.
     * @return The created Account object if registration is successful, null otherwise.
     */
    public static Account createAccount(Account account) {
        // Initialize a connection to the database
        try (Connection connection = ConnectionUtil.getConnection()) {
            // SQL query to insert a new user account into the database
            String query = "INSERT INTO account (username, password) VALUES (?, ?)";
            
            try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, account.getUsername());
                statement.setString(2, account.getPassword());
                
                // Execute the insert query
                int rowsInserted = statement.executeUpdate();
                
                if (rowsInserted > 0) {
                    // Registration successful, retrieve the generated account_id
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int accountId = generatedKeys.getInt(1);
                            // Set the account_id for the newly created account
                            account.setAccount_id(accountId);
                            return account;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Registration failed
        return null;
    }


/**
     * Authenticates a user by checking if the provided username and password match a user in the database.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The authenticated Account object if successful, null otherwise.
     */
    public static Account authenticateAccount(String username, String password) {
        // Initialize a connection to the database
        try {Connection connection = ConnectionUtil.getConnection(); {
            // SQL query to check if a user with the provided username and password exists
            String query = "SELECT * FROM account WHERE username = ? AND password = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                
                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // User with the provided credentials exists
                        int accountId = resultSet.getInt("account_id");
                        String usernameFromDb = resultSet.getString("username");
                        String passwordFromDb = resultSet.getString("password");
                        
                        // Create an Account object for the authenticated user
                        Account authenticatedAccount = new Account(accountId, usernameFromDb, passwordFromDb);
                        return authenticatedAccount;
                    }
                }
            }
          }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    

}