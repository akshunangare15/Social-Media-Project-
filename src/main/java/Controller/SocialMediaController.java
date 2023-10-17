    package Controller;

    import io.javalin.Javalin;
    import io.javalin.http.Context;
    import io.javalin.http.NotFoundResponse;
    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.JsonMappingException;
    import com.fasterxml.jackson.databind.ObjectMapper;

    import DAO.AccountDAO;
    import Model.Account;
    import Model.Message;
    import Util.ConnectionUtil;

    import java.sql.*;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.List;


    /**
     * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
     * found in readme.md as well as the test cases. You should
     * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
     */

    public class SocialMediaController {

        // private AccountDAO accountDAO = new AccountDAO();
        // private ObjectMapper objectMapper = new ObjectMapper();

        /**
         * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
         * suite must receive a Javalin object from this method.
         * @return a Javalin app object which defines the behavior of the Javalin controller.
         */
    
        public Javalin startAPI() {
            Javalin app = Javalin.create();
            app.post("/login", this::loginHandler);
            app.post("/register", this::registerHandler);
            app.get("/messages", this::getAllMessagesHandler);
            app.post("/messages", this::createNewMessageHandler);
            app.get("/messages/{message_id}", this::getMessageByMessageIdHandler);
            app.delete("/messages/{message_id}", this::deleteMessageByMessageIdHandler);
            app.patch("/messages/{message_id}", this::updateMessageByMessageIdHandler);
            app.get("/accounts/{account_id}/{messages_id}", this::getMessagesByAccountIdHandler);
            return app;
        }

        
            /**
             * Handles user registration by deserializing the JSON request body into an Account object,
             * validating the registration data, and creating a new account.
             * @param context The Javalin context object.
             **/

            
                private void registerHandler(Context context) throws JsonProcessingException, JsonMappingException {
                    ObjectMapper mapper = new ObjectMapper();
            
                    // Deserialize the JSON request body into an Account object
                    Account newAccount = mapper.readValue(context.body(), Account.class);
            
                    if (isValidRegistration(newAccount)) {
                        // Registration data is valid, attempt to create a new account
                        Account createdAccount = AccountDAO.createAccount(newAccount);
            
                        if (createdAccount != null) {
                            // Account creation successful, return the created account details
                            context.json(mapper.writeValueAsString(createdAccount));
                        } else {
                            // Internal server error, you might want to change this based on your actual error handling
                            context.status(500);
                        }
                    } else {
                        // Bad Request - Registration data is not valid
                        context.status(400);
                    }
                }
            
            
            /**
             * Validates the registration data for a new account.
             * @param account The Account object to be validated.
             * @return true if the registration data is valid, false otherwise.
             */
            private boolean isValidRegistration(Account account) {
                if (account != null && !account.getUsername().isBlank() && account.getPassword().length() >= 4) {
                    
                }
                return false;
            }
            

        /**
         * Handles user login by deserializing the JSON request body into an Account object,
         * authenticating the account, and returning the account details if authentication is successful.
         * @param context The Javalin context object.
         */
        private void loginHandler(Context context) throws JsonProcessingException, JsonMappingException {
            ObjectMapper mapper = new ObjectMapper();
            
            // Deserialize the JSON request body into an Account object
            Account loginAccount = mapper.readValue(context.body(), Account.class);
            
            // Check if the login credentials are valid
            Account authenticatedAccount = AccountDAO.authenticateAccount(loginAccount.getUsername(), loginAccount.getPassword());
            
            if (authenticatedAccount != null) {
                // Successfully authenticated, return the account details
                context.json(mapper.writeValueAsString(authenticatedAccount));
            } else {
                // Authentication failed, return a 401 Unauthorized status
                context.status(401);
            }
        }
        

        /**
         * Handles the creation of a new message by deserializing the JSON request body into a Message object,
         * validating the message data, and saving the message in the database.
         *
         * @param context The Javalin context object.
         */
        private void createNewMessageHandler(Context context) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Message newMessage = mapper.readValue(context.body(), Message.class);

                if (!newMessage.getMessage_text().isBlank() && newMessage.getMessage_text().length() < 255) {
                    Message savedMessage = saveMessageInDatabase(newMessage);

                    if (savedMessage != null) {
                        context.json(savedMessage);
                    } else {
                        context.status(400);
                    }
                } else {
                    context.status(400).json("");
                }
            } catch (Exception e) {
                context.status(400).json("");
            }
        }

        /**
         * Helper method to save a new message in the database.
         *
         * @param newMessage The Message object to be saved.
         * @return The saved Message object if successful, null otherwise.
         */
        private Message saveMessageInDatabase(Message newMessage) {
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, newMessage.getPosted_by());
                    statement.setString(2, newMessage.getMessage_text());
                    statement.setLong(3, newMessage.getTime_posted_epoch());

                    int rowsInserted = statement.executeUpdate();

                    if (rowsInserted > 0) {
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int messageId = generatedKeys.getInt(1);
                            return new Message(messageId, newMessage.getPosted_by(), newMessage.getMessage_text(), newMessage.getTime_posted_epoch());
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Handles the retrieval of all messages from the database.
         *
         * @param context The Javalin context object.
         */
        private void getAllMessagesHandler(Context context) {
            List<Message> messages = retrieveAllMessagesFromDatabase();

            if (messages != null) {
                context.json(messages);
            } else {
                context.json(Collections.emptyList());
            }
        }

        /**
         * Handles the retrieval of a message by its message_id from the database.
         *
         * @param context The Javalin context object.
         */
        private void getMessageByMessageIdHandler(Context context) {
            try {
                String messageIdParam = context.pathParam("message_id");
                int messageId = Integer.parseInt(messageIdParam);

                Message message = getMessageByMessageIdFromDatabase(messageId);

                if (message != null) {
                    context.json(message);
                } else {
                    context.json("");
                }
            } catch (NumberFormatException e) {
                context.status(200).json("");
            }
        }

        /**
         * Handles the deletion of a message by its message_id from the database.
         *
         * @param context The Javalin context object.
         */
        private void deleteMessageByMessageIdHandler(Context context) {
            try {
                // Extract the message_id from the path parameter
                String messageIdParam = context.pathParam("message_id");
                int messageId = Integer.parseInt(messageIdParam);

                Message existingMessage = getMessageByMessageIdFromDatabase(messageId);

                if (existingMessage != null) {
                    if (deleteMessageInDatabase(messageId)) {
                        context.json(existingMessage);
                    } else {
                        context.json(messageId);
                    }
                } else {
                    context.status(200); // Use a 204 status for resource not found (No Content)
                }
            } catch (NumberFormatException e) {
                context.status(400);
            } catch (Exception e) {
                context.status(400);
            }
        }

        /**
         * Helper method to delete a message from the database.
         *
         * @param messageId The ID of the message to be deleted.
         * @return true if the message is deleted successfully, false otherwise.
         */
        private boolean deleteMessageInDatabase(int messageId) {
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "DELETE FROM message WHERE message_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, messageId);
                    int rowsDeleted = statement.executeUpdate();
                    return rowsDeleted > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Handles the update of a message by its message_id in the database.
         *
         * @param context The Javalin context object.
         */
        private void updateMessageByMessageIdHandler(Context context) {
            try {
                String messageIdParam = context.pathParam("message_id");
                int messageId = Integer.parseInt(messageIdParam);

                Message existingMessage = getMessageByMessageIdFromDatabase(messageId);

                if (existingMessage != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    Message updatedMessage = mapper.readValue(context.body(), Message.class);

                    if (!updatedMessage.getMessage_text().isBlank() && updatedMessage.getMessage_text().length() < 255) {
                        existingMessage.setMessage_text(updatedMessage.getMessage_text());

                        if (updateMessageInDatabase(existingMessage)) {
                            context.json(existingMessage);
                        } else {
                            context.json("");
                        }
                    } else {
                        context.status(400).json("");
                    }
                } else {
                    throw new NotFoundResponse(""); // Updated to use a 404 response
                }
            } catch (NumberFormatException e) {
                context.status(400).json("");
            } catch (Exception e) {
                context.status(400).json("");
            }
        }

        /**
         * Helper method to update a message in the database.
         *
         * @param existingMessage The existing Message object to be updated.
         * @return true if the message is updated successfully, false otherwise.
         */
        private boolean updateMessageInDatabase(Message existingMessage) {
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "UPDATE message SET message_text = ? WHERE message_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, existingMessage.getMessage_text());
                    statement.setInt(2, existingMessage.getMessage_id());
                    int rowsUpdated = statement.executeUpdate();
                    return rowsUpdated >= 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Handles the retrieval of all messages from a specific user by account_id.
         *
         * @param context The Javalin context object.
         */
        private void getMessagesByAccountIdHandler(Context context) {
            try {
                String accountIdParam = context.pathParam("account_id");
                int accountId = Integer.parseInt(accountIdParam);

                List<Message> messages = getMessagesByAccountIdFromDatabase(accountId);

                if (messages != null) {
                    context.json(messages);
                } else {
                    context.json(Collections.emptyList());
                }
            } catch (NumberFormatException e) {
                context.json("");
            }
        }

        /**
         * Helper method to retrieve a message by its message_id from the database.
         *
         * @param messageId The ID of the message to be retrieved.
         * @return The retrieved Message object if found, null otherwise.
         */
        private List<Message> retrieveAllMessagesFromDatabase() {
            List<Message> messages = new ArrayList<>();
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "SELECT * FROM message";
                try (Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query)) {
                    while (resultSet.next()) {
                        int message_id = resultSet.getInt("message_id");
                        int posted_by = resultSet.getInt("posted_by");
                        String message_text = resultSet.getString("message_text");
                        long time_posted_epoch = resultSet.getLong("time_posted_epoch");
                        Message message = new Message(message_id, posted_by, message_text, time_posted_epoch);
                        messages.add(message);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return messages;
        }

        /**
         * Helper method to retrieve a message by its message_id from the database.
         *
         * @param messageId The ID of the message to be retrieved.
         * @return The retrieved Message object if found, null otherwise.
         */
        private Message getMessageByMessageIdFromDatabase(int messageId) {
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "SELECT * FROM message WHERE message_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, messageId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int message_id = resultSet.getInt("message_id");
                            int posted_by = resultSet.getInt("posted_by");
                            String message_text = resultSet.getString("message_text");
                            long time_posted_epoch = resultSet.getLong("time_posted_epoch");
                            return new Message(message_id, posted_by, message_text, time_posted_epoch);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Helper method to retrieve messages by account_id from the database.
         *
         * @param accountId The ID of the account for which messages are to be retrieved.
         * @return A list of Message objects retrieved by account_id from the database.
         */
        private List<Message> getMessagesByAccountIdFromDatabase(int accountId) {
            List<Message> messages = new ArrayList<>();
            try {
                Connection connection = ConnectionUtil.getConnection();
                String query = "SELECT * FROM message WHERE posted_by = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, accountId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            int message_id = resultSet.getInt("message_id");
                            int posted_by = resultSet.getInt("posted_by");
                            String message_text = resultSet.getString("message_text");
                            long time_posted_epoch = resultSet.getLong("time_posted_epoch");
                            Message message = new Message(message_id, posted_by, message_text, time_posted_epoch);
                            messages.add(message);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return messages;
        }
    }
