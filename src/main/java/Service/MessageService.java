    package Service;

    import DAO.MessageDAO;
    import Model.Message;

    import java.util.List;

    public class MessageService {
        private MessageDAO messageDAO;

        // Constructor to initialize the messageDAO
        public MessageService(MessageDAO messageDAO) {
            this.messageDAO = messageDAO;
        }

        /**
         * Create a new message.
         *
         * @param message The message to be created.
         * @return The created message with an assigned message_id.
         */
        public Message createMessage(Message message) {
            return MessageDAO.saveMessage(message);
        }

        /**
         * Get all messages.
         *
         * @return A list of all messages.
         */
        public List<Message> getAllMessages() {
            return messageDAO.getAllMessages();
        }

        /**
         * Get a message by its message_id.
         *
         * @param messageId The unique identifier of the message.
         * @return The message corresponding to the provided message_id.
         */
        public Message getMessageById(int messageId) {
            return messageDAO.getMessageById(messageId);
        }

        /**
         * Delete a message by its message_id.
         *
         * @param messageId The unique identifier of the message to be deleted.
         * @return The deleted message, or null if not found or not deleted successfully.
         */
        public Message deleteMessageById(int messageId) {
            return messageDAO.deleteMessageByMessageIdFromDatabase(messageId);
        }

        /**
         * Update a message.
         *
         * @param message The message to be updated.
         * @return The updated message.
         */
        public Message updateMessage(Message message) {
            return MessageDAO.saveMessage(message);
        }

        /**
         * Get messages by account ID.
         *
         * @param accountId The unique identifier of the account.
         * @return The messages posted by the specified account.
         */
        public Message getMessagesByAccountId(int accountId) {
            return messageDAO.getMessageById(accountId);
        }
    }
