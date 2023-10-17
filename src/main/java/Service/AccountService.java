    package Service;

    import DAO.AccountDAO;
    import Model.Account;

    public class AccountService {
        
        // Constructor to initialize the accountDAO
        public AccountService(AccountDAO accountDAO) {
        }

        /**
         * Create a new user account.
         *
         * @param account The account to be created.
         * @return The created account with an assigned account_id.
         */
        public static  Account createAccount(Account username) {
            return AccountDAO.createAccount(username);
        }

        /**
         * Authenticate a user based on username and password.
         *
         * @param username The username of the user.
         * @param password The password of the user.
         * @return The authenticated account if successful, otherwise null.
         */
        public  Account login(String username, String password) {
            return AccountDAO.authenticateAccount(username, password);
        }

    }
