package DAO;

import Model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private static Connection connection;

    // Constructor to initialize the connection
    public MessageDAO(Connection connection) {
        //this.connection = connection;
    }

    /**
     * Save a message in the database.
     *
     * @param message The message to be saved.
     * @return The saved message with an assigned message_id.
     */
    public static Message saveMessage(Message message) {
        String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, message.getPosted_by());
            statement.setString(2, message.getMessage_text());
            statement.setLong(3, message.getTime_posted_epoch());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int messageId = generatedKeys.getInt(1);
                    return new Message(messageId, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get all messages from the database.
     *
     * @return A list of all messages.
     */
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message";
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int messageId = resultSet.getInt("message_id");
                int postedBy = resultSet.getInt("posted_by");
                String messageText = resultSet.getString("message_text");
                long timePostedEpoch = resultSet.getLong("time_posted_epoch");
                Message message = new Message(messageId, postedBy, messageText, timePostedEpoch);
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * Delete a message from the database by its message_id.
     *
     * @param messageId The unique identifier of the message to be deleted.
     * @return The deleted message, or null if not found or not deleted successfully.
     */
    public Message deleteMessageByMessageIdFromDatabase(int messageId) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, messageId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                // Message was deleted successfully
                return getMessageById(messageId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Message with the specified ID does not exist or couldn't be deleted
        return null;
    }

    /**
     * Get a message by its message_id from the database.
     *
     * @param messageId The unique identifier of the message.
     * @return The message corresponding to the provided message_id, or null if not found.
     */
    public Message getMessageById(int messageId) {
        String sql = "SELECT * FROM message WHERE message_id = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, messageId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int postedBy = resultSet.getInt("posted_by");
                String messageText = resultSet.getString("message_text");
                long timePostedEpoch = resultSet.getLong("time_posted_epoch");
                return new Message(postedBy, messageText, timePostedEpoch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Message with the specified ID does not exist
        return null;
    }

    // // Static method to delete a message by its message_id
    // public static Message deleteMessageByMessageId(int messageId) {
    //     return null;
    // }
}
