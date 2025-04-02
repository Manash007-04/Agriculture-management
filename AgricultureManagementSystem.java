import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class AgricultureManagementSystem {

    private static final String USER_DATA_FILE = "user.txt";
    private static String[] CROPS = {"Wheat", "Corn", "Bajra", "Jute", "Cotton"};
    private static double[] PRICES = {2000.0, 1800.0, 2200.0, 2500.0, 3000.0};
    private static String[] SUBSIDIES = {
        "Subsidy 1: 50% subsidy on fertilizers.",
        "Subsidy 2: Interest-free loans for small farmers.",
        "Subsidy 3: Free seeds for organic farming."
    };
    private static String[] SUBSIDY_DETAILS = {
        "Details for Subsidy 1: This subsidy provides a 50% discount on fertilizers for all registered farmers. Valid until December 2023.",
        "Details for Subsidy 2: Small farmers can avail interest-free loans up to ₹1,00,000. Contact your nearest agriculture office for more details.",
        "Details for Subsidy 3: Free seeds for organic farming are available for farmers practicing sustainable agriculture. Apply online at www.agri-subsidy.gov.in."
    };
    private static String[][] CROP_STEPS = {
        {"Wheat Steps: 1. Prepare well-drained soil. 2. Sow seeds in November. 3. Irrigate regularly. 4. Harvest in March-April."},
        {"Corn Steps: 1. Choose fertile soil. 2. Sow seeds in May-June. 3. Provide adequate water. 4. Harvest after 90-120 days."},
        {"Bajra Steps: 1. Use sandy loam soil. 2. Sow seeds in July. 3. Ensure proper drainage. 4. Harvest after 60-90 days."},
        {"Jute Steps: 1. Use alluvial soil. 2. Sow seeds in March-April. 3. Keep soil moist. 4. Harvest after 120-150 days."},
        {"Cotton Steps: 1. Use black soil. 2. Sow seeds in May-June. 3. Provide regular irrigation. 4. Harvest after 150-180 days."}
    };

    private static HashMap<String, User> users = new HashMap<>();
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadUserData();

        // Add default admin user if not exists
        if (!users.containsKey("admin")) {
            users.put("admin", new User("admin", "admin123", "Administrator", true));
        }

        System.out.println("==============================================");
        System.out.println("    AGRICULTURE MANAGEMENT SYSTEM");
        System.out.println("==============================================");

        while (true) {
            System.out.println("\nMain Options:");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 3);

            switch (choice) {
                case 1:
                    login();
                    if (currentUser != null) {
                        if (currentUser.isAdmin()) {
                            showAdminMenu();
                        } else {
                            showMainMenu();
                        }
                    }
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    saveUserData();
                    System.out.println("\nThank you for using the Agriculture Management System!");
                    System.exit(0);
            }
        }
    }

    private static void loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    User user = new User(
                        parts[0].trim(), // username
                        parts[1].trim(), // password
                        parts[2].trim(), // fullName
                        Boolean.parseBoolean(parts[3].trim()) // isAdmin
                    );
                    
                    // Land details (if available)
                    if (parts.length >= 7) {
                        user.setLandDetails(
                            Double.parseDouble(parts[4].trim()), // landSize
                            parts[5].trim(), // location
                            parts[6].trim()  // soilType
                        );
                    }
                    
                    // Subsidy applications (if available)
                    if (parts.length >= 8 && !parts[7].trim().isEmpty()) {
                        for (String app : parts[7].trim().split(",")) {
                            user.addSubsidyApplication(Integer.parseInt(app.trim()));
                        }
                    }
                    
                    // Approved subsidies (if available)
                    if (parts.length >= 9 && !parts[8].trim().isEmpty()) {
                        for (String approved : parts[8].trim().split(",")) {
                            user.getApprovedSubsidies().add(Integer.parseInt(approved.trim()));
                        }
                    }
                    
                    users.put(user.getUsername(), user);
                }
            }
            System.out.println("User data loaded successfully from text file.");
        } catch (FileNotFoundException e) { // if file does not exists
            System.out.println("No existing user data found. Starting with empty database.");
        } catch (Exception e) { //if currpted file or security issues 
            System.out.println("Error loading user data: " + e.getMessage());
        }
    }

    private static void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
            for (User user : users.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(user.getUsername()).append("|");
                sb.append(user.getPassword()).append("|");
                sb.append(user.getFullName()).append("|");
                sb.append(user.isAdmin()).append("|");
                sb.append(user.getLandSize()).append("|");
                sb.append(user.getLocation()).append("|");
                sb.append(user.getSoilType()).append("|");
                
                // Subsidy applications
                sb.append(String.join(",", 
                    user.getSubsidyApplications().stream()
                        .map(String::valueOf)
                        .toArray(String[]::new))).append("|");
                
                // Approved subsidies
                sb.append(String.join(",", 
                    user.getApprovedSubsidies().stream()
                        .map(String::valueOf)
                        .toArray(String[]::new)));
                
                writer.write(sb.toString());
                writer.newLine();
            }
            System.out.println("User data saved successfully in text format.");
        } catch (Exception e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static void login() {
        System.out.println("\n=== Login ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (users.containsKey(username)) {
            User user = users.get(username);
            if (user.authenticate(password)) {
                currentUser = user;
                System.out.println("\nLogin successful! Welcome, " + username + "!");
            } else {
                System.out.println("\nIncorrect password!");
            }
        } else {
            System.out.println("\nUsername not found!");
        }
    }

    private static void register() {
        System.out.println("\n=== Register ===");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("\nUsername already exists!");
            return;
        }

        System.out.print("Choose a password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your full name: ");
        String fullName = scanner.nextLine();

        users.put(username, new User(username, password, fullName, false));
        System.out.println("\nRegistration successful! You can now login.");
    }

    private static void showMainMenu() {
        while (currentUser != null) {
            System.out.println("\n==============================================");
            System.out.println("    MAIN MENU - Welcome, " + currentUser.getFullName());
            System.out.println("==============================================");
            System.out.println("1. View/Update Land Details");
            System.out.println("2. View Crop Prices");
            System.out.println("3. View Government Subsidies");
            System.out.println("4. View Crop Growing Steps");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 5);

            switch (choice) {
                case 1:
                    manageLandDetails();
                    break;
                case 2:
                    displayCropPrices();
                    break;
                case 3:
                    manageSubsidies();
                    break;
                case 4:
                    viewCropSteps();
                    break;
                case 5:
                    currentUser = null;
                    System.out.println("\nLogged out successfully!");
                    return;
            }
        }
    }

    private static void showAdminMenu() {
        while (currentUser != null) {
            System.out.println("\n==============================================");
            System.out.println("    ADMIN MENU - Welcome, " + currentUser.getFullName());
            System.out.println("==============================================");
            System.out.println("1. Manage Crop Prices");
            System.out.println("2. Manage Subsidies");
            System.out.println("3. View All Users");
            System.out.println("4. Manage Subsidy Applications");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 5);

            switch (choice) {
                case 1:
                    manageCropPrices();
                    break;
                case 2:
                    manageSubsidyData();
                    break;
                case 3:
                    viewAllUsers();
                    break;
                case 4:
                    manageApplications();
                    break;
                case 5:
                    currentUser = null;
                    System.out.println("\nLogged out successfully!");
                    return;
            }
        }
    }

    private static void manageCropPrices() {
        while (true) {
            System.out.println("\n=== Manage Crop Prices ===");
            displayCropPrices();

            System.out.println("\nOptions:");
            System.out.println("1. Update Crop Price");
            System.out.println("2. Add New Crop");
            System.out.println("3. Remove Crop");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 4);

            switch (choice) {
                case 1:
                    updateCropPrice();
                    break;
                case 2:
                    addNewCrop();
                    break;
                case 3:
                    removeCrop();
                    break;
                case 4:
                    return;
            }
        }
    }

    private static void updateCropPrice() {
        System.out.println("\nSelect crop to update price:");
        for (int i = 0; i < CROPS.length; i++) {
            System.out.println((i + 1) + ". " + CROPS[i] + " (Current price: ₹" + PRICES[i] + ")");
        }
        System.out.print("Enter crop number: ");
        int cropNum = getIntInput(1, CROPS.length);

        System.out.print("Enter new price for " + CROPS[cropNum - 1] + ": ");
        double newPrice = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        PRICES[cropNum - 1] = newPrice;
        System.out.println("Price updated successfully!");
    }

    private static void addNewCrop() {
        System.out.print("\nEnter new crop name: ");
        String newCrop = scanner.nextLine();

        System.out.print("Enter price for " + newCrop + ": ");
        double newPrice = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        // Add to crops array
        String[] newCrops = new String[CROPS.length + 1];
        System.arraycopy(CROPS, 0, newCrops, 0, CROPS.length);
        newCrops[CROPS.length] = newCrop;
        CROPS = newCrops;

        // Add to prices array
        double[] newPrices = new double[PRICES.length + 1];
        System.arraycopy(PRICES, 0, newPrices, 0, PRICES.length);
        newPrices[PRICES.length] = newPrice;
        PRICES = newPrices;

        // Add default growing steps
        String[][] newCropSteps = new String[CROP_STEPS.length + 1][];
        System.arraycopy(CROP_STEPS, 0, newCropSteps, 0, CROP_STEPS.length);
        newCropSteps[CROP_STEPS.length] = new String[]{newCrop + " Steps: 1. Default step 1. 2. Default step 2. 3. Default step 3."};
        CROP_STEPS = newCropSteps;

        System.out.println("New crop added successfully!");
    }

    private static void removeCrop() {
        System.out.println("\nSelect crop to remove:");
        for (int i = 0; i < CROPS.length; i++) {
            System.out.println((i + 1) + ". " + CROPS[i]);
        }
        System.out.print("Enter crop number: ");
        int cropNum = getIntInput(1, CROPS.length);

        // Confirm deletion
        System.out.print("Are you sure you want to remove " + CROPS[cropNum - 1] + "? (yes/no): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            // Remove from crops array
            String[] newCrops = new String[CROPS.length - 1];
            for (int i = 0, j = 0; i < CROPS.length; i++) {
                if (i != cropNum - 1) {
                    newCrops[j++] = CROPS[i];
                }
            }
            CROPS = newCrops;

            // Remove from prices array
            double[] newPrices = new double[PRICES.length - 1];
            for (int i = 0, j = 0; i < PRICES.length; i++) {
                if (i != cropNum - 1) {
                    newPrices[j++] = PRICES[i];
                }
            }
            PRICES = newPrices;

            // Remove from crop steps
            String[][] newCropSteps = new String[CROP_STEPS.length - 1][];
            for (int i = 0, j = 0; i < CROP_STEPS.length; i++) {
                if (i != cropNum - 1) {
                    newCropSteps[j++] = CROP_STEPS[i];
                }
            }
            CROP_STEPS = newCropSteps;

            System.out.println("Crop removed successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private static void manageSubsidyData() {
        while (true) {
            System.out.println("\n=== Manage Subsidies ===");
            System.out.println("Current Subsidies:");
            for (int i = 0; i < SUBSIDIES.length; i++) {
                System.out.println((i + 1) + ". " + SUBSIDIES[i]);
            }

            System.out.println("\nOptions:");
            System.out.println("1. Update Subsidy");
            System.out.println("2. Add New Subsidy");
            System.out.println("3. Remove Subsidy");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 4);

            switch (choice) {
                case 1:
                    updateSubsidy();
                    break;
                case 2:
                    addNewSubsidy();
                    break;
                case 3:
                    removeSubsidy();
                    break;
                case 4:
                    return;
            }
        }
    }

    private static void updateSubsidy() {
        System.out.println("\nSelect subsidy to update:");
        for (int i = 0; i < SUBSIDIES.length; i++) {
            System.out.println((i + 1) + ". " + SUBSIDIES[i]);
        }
        System.out.print("Enter subsidy number: ");
        int subNum = getIntInput(1, SUBSIDIES.length);

        System.out.println("Current subsidy: " + SUBSIDIES[subNum - 1]);
        System.out.println("Current details: " + SUBSIDY_DETAILS[subNum - 1]);

        System.out.print("\nEnter new subsidy description: ");
        String newSub = scanner.nextLine();
        System.out.print("Enter new subsidy details: ");
        String newDetails = scanner.nextLine();

        SUBSIDIES[subNum - 1] = newSub;
        SUBSIDY_DETAILS[subNum - 1] = newDetails;

        System.out.println("Subsidy updated successfully!");
    }

    private static void addNewSubsidy() {
        System.out.print("\nEnter new subsidy description: ");
        String newSub = scanner.nextLine();

        System.out.print("Enter subsidy details: ");
        String newDetails = scanner.nextLine();

        // Add to subsidies array
        String[] newSubs = new String[SUBSIDIES.length + 1];
        System.arraycopy(SUBSIDIES, 0, newSubs, 0, SUBSIDIES.length);
        newSubs[SUBSIDIES.length] = newSub;
        SUBSIDIES = newSubs;

        // Add to details array
        String[] newDetailsArr = new String[SUBSIDY_DETAILS.length + 1];
        System.arraycopy(SUBSIDY_DETAILS, 0, newDetailsArr, 0, SUBSIDY_DETAILS.length);
        newDetailsArr[SUBSIDY_DETAILS.length] = newDetails;
        SUBSIDY_DETAILS = newDetailsArr;

        System.out.println("New subsidy added successfully!");
    }

    private static void removeSubsidy() {
        System.out.println("\nSelect subsidy to remove:");
        for (int i = 0; i < SUBSIDIES.length; i++) {
            System.out.println((i + 1) + ". " + SUBSIDIES[i]);
        }
        System.out.print("Enter subsidy number: ");
        int subNum = getIntInput(1, SUBSIDIES.length);

        // Confirm deletion
        System.out.print("Are you sure you want to remove this subsidy? (yes/no): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            // Remove from subsidies array
            String[] newSubs = new String[SUBSIDIES.length - 1];
            for (int i = 0, j = 0; i < SUBSIDIES.length; i++) {
                if (i != subNum - 1) {
                    newSubs[j++] = SUBSIDIES[i];
                }
            }
            SUBSIDIES = newSubs;

            // Remove from details array
            String[] newDetails = new String[SUBSIDY_DETAILS.length - 1];
            for (int i = 0, j = 0; i < SUBSIDY_DETAILS.length; i++) {
                if (i != subNum - 1) {
                    newDetails[j++] = SUBSIDY_DETAILS[i];
                }
            }
            SUBSIDY_DETAILS = newDetails;

            // Remove from all users' applications
            for (User user : users.values()) {
                List<Integer> apps = user.getSubsidyApplications();
                for (int i = 0; i < apps.size(); i++) {
                    if (apps.get(i) == subNum) {
                        apps.remove(i);
                        i--;
                    } else if (apps.get(i) > subNum) {
                        apps.set(i, apps.get(i) - 1);
                    }
                }
            }

            System.out.println("Subsidy removed successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private static void viewAllUsers() {
        System.out.println("\n=== All Registered Users ===");
 
        for (User user : users.values()) {
            if (!user.isAdmin()) {
                System.out.printf("%-15s %-20s %-10.2f %-15s %-15s%n",
                        user.getUsername(),
                        user.getFullName(),
                        user.getLandSize(),
                        user.getLocation(),
                        user.getSoilType());
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void manageApplications() {
        while (true) {
            System.out.println("\n=== Manage Subsidy Applications ===");
            System.out.println("1. View All Applications");
            System.out.println("2. Update Application Status");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 3);

            switch (choice) {
                case 1:
                    viewAllApplications();
                    break;
                case 2:
                    updateApplicationStatus();
                    break;
                case 3:
                    return;
            }
        }
    }

    private static void viewAllApplications() {
        System.out.println("\n=== All Subsidy Applications ===");
        boolean found = false;

        for (User user : users.values()) {
            if (!user.isAdmin() && !user.getSubsidyApplications().isEmpty()) {
                found = true;
                System.out.println("\nUser: " + user.getUsername() + " (" + user.getFullName() + ")");
                System.out.println("Land Size: " + user.getLandSize() + " acres");
                System.out.println("Location: " + user.getLocation());
                System.out.println("Applications:");

                for (int appNum : user.getSubsidyApplications()) {
                    System.out.println("- " + SUBSIDIES[appNum - 1]);
                    System.out.println("  Status: " + (user.getApprovedSubsidies().contains(appNum) ? "Approved" : "Pending"));
                }
            }
        }

        if (!found) {
            System.out.println("No applications found.");
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void updateApplicationStatus() {
        System.out.print("\nEnter username to manage applications: ");
        String username = scanner.nextLine();

        if (!users.containsKey(username) || users.get(username).isAdmin()) {
            System.out.println("User not found or is an admin!");
            return;
        }

        User user = users.get(username);
        if (user.getSubsidyApplications().isEmpty()) {
            System.out.println("This user has no applications.");
            return;
        }

        System.out.println("\nApplications for " + username + ":");
        List<Integer> apps = user.getSubsidyApplications();
        for (int i = 0; i < apps.size(); i++) {
            System.out.println((i + 1) + ". " + SUBSIDIES[apps.get(i) - 1]);
            System.out.println("   Status: " + (user.getApprovedSubsidies().contains(apps.get(i)) ? "Approved" : "Pending"));
        }

        System.out.print("Select application to update (1-" + apps.size() + "): ");
        int appIndex = getIntInput(1, apps.size());
        int appNum = apps.get(appIndex - 1);

        System.out.println("\nSelected application: " + SUBSIDIES[appNum - 1]);
        System.out.println("Current status: " + (user.getApprovedSubsidies().contains(appNum) ? "Approved" : "Pending"));

        System.out.println("\nOptions:");
        System.out.println("1. Approve Application");
        System.out.println("2. Reject Application");
        System.out.println("3. Cancel");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 3);

        switch (choice) {
            case 1:
                if (!user.getApprovedSubsidies().contains(appNum)) {
                    user.getApprovedSubsidies().add(appNum);
                    System.out.println("Application approved!");
                } else {
                    System.out.println("Application is already approved.");
                }
                break;
            case 2:
                if (user.getApprovedSubsidies().contains(appNum)) {
                    user.getApprovedSubsidies().remove((Integer) appNum);
                    System.out.println("Application rejected!");
                } else {
                    System.out.println("Application is already pending.");
                }
                break;
            case 3:
                System.out.println("Operation cancelled.");
                break;
        }
    }

    private static void manageLandDetails() {
        System.out.println("\n=== Land Details ===");
        if (currentUser.getLandSize() == 0) {
            System.out.println("No land details entered yet.");
        } else {
            System.out.println("Current Land Details:");
            System.out.println("Size: " + currentUser.getLandSize() + " acres");
            System.out.println("Location: " + currentUser.getLocation());
            System.out.println("Soil Type: " + currentUser.getSoilType());
        }

        System.out.println("\nOptions:");
        System.out.println("1. Enter/Update Land Details");
        System.out.println("2. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, 2);

        if (choice == 1) {
            System.out.print("\nEnter land size (in acres): ");
            double landSize = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            System.out.print("Enter location: ");
            String location = scanner.nextLine();
            System.out.print("Enter soil type: ");
            String soilType = scanner.nextLine();

            currentUser.setLandDetails(landSize, location, soilType);
            System.out.println("\nLand details saved successfully!");
        }
    }

    private static void displayCropPrices() {
        System.out.println("\n=== Current Crop Prices (per quintal) ===");
        System.out.println("+--------+------------+");
        System.out.println("| Crop   | Price (₹) |");
        System.out.println("+--------+------------+");
        for (int i = 0; i < CROPS.length; i++) {
            System.out.printf("| %-6s | %10.2f |\n", CROPS[i], PRICES[i]);
        }
        System.out.println("+--------+------------+");

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void manageSubsidies() {
        while (true) {
            System.out.println("\n=== Government Subsidies ===");
            System.out.println("Available Subsidies:");
            for (int i = 0; i < SUBSIDIES.length; i++) {
                System.out.println((i + 1) + ". " + SUBSIDIES[i]);
            }

            System.out.println("\nOptions:");
            System.out.println("1. View Subsidy Details");
            System.out.println("2. Apply for Subsidy");
            System.out.println("3. View My Subsidy Applications");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 4);

            switch (choice) {
                case 1:
                    viewSubsidyDetails();
                    break;
                case 2:
                    applyForSubsidy();
                    break;
                case 3:
                    viewMyApplications();
                    break;
                case 4:
                    return;
            }
        }
    }

    private static void viewSubsidyDetails() {
        System.out.print("\nEnter the subsidy number to view details: ");
        int subsidyNum = getIntInput(1, SUBSIDY_DETAILS.length);

        System.out.println("\n=== Subsidy Details ===");
        System.out.println(SUBSIDY_DETAILS[subsidyNum - 1]);

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void applyForSubsidy() {
        System.out.print("\nEnter the subsidy number you want to apply for: ");
        int subsidyNum = getIntInput(1, SUBSIDIES.length);

        if (currentUser.hasAppliedForSubsidy(subsidyNum)) {
            System.out.println("\nYou have already applied for this subsidy!");
            return;
        }

        System.out.println("\nYou are applying for: " + SUBSIDIES[subsidyNum - 1]);
        System.out.print("Do you confirm? (yes/no): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            currentUser.addSubsidyApplication(subsidyNum);
            System.out.println("\nApplication submitted successfully!");
        } else {
            System.out.println("\nApplication cancelled.");
        }
    }

    private static void viewMyApplications() {
        System.out.println("\n=== My Subsidy Applications ===");
        if (currentUser.getSubsidyApplications().isEmpty()) {
            System.out.println("You have no active applications.");
        } else {
            for (int app : currentUser.getSubsidyApplications()) {
                System.out.println("- " + SUBSIDIES[app - 1]);
                System.out.println("  Status: " + (currentUser.getApprovedSubsidies().contains(app) ? "Approved" : "Pending"));
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewCropSteps() {
        System.out.println("\n=== Crop Growing Steps ===");
        System.out.println("Select a crop:");
        for (int i = 0; i < CROPS.length; i++) {
            System.out.println((i + 1) + ". " + CROPS[i]);
        }
        System.out.println((CROPS.length + 1) + ". Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = getIntInput(1, CROPS.length + 1);

        if (choice <= CROPS.length) {
            System.out.println("\n=== Growing Steps for " + CROPS[choice - 1] + " ===");
            System.out.println(CROP_STEPS[choice - 1][0]);

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String fullName;
    private double landSize;
    private String location;
    private String soilType;
    private List<Integer> subsidyApplications = new ArrayList<>();
    private List<Integer> approvedSubsidies = new ArrayList<>();
    private boolean isAdmin;

    public User(String username, String password, String fullName) {
        this(username, password, fullName, false);
    }

    public User(String username, String password, String fullName, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.isAdmin = isAdmin;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public void setLandDetails(double landSize, String location, String soilType) {
        this.landSize = landSize;
        this.location = location;
        this.soilType = soilType;
    }

    public void addSubsidyApplication(int subsidyNumber) {
        subsidyApplications.add(subsidyNumber);
    }

    public boolean hasAppliedForSubsidy(int subsidyNumber) {
        return subsidyApplications.contains(subsidyNumber);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public double getLandSize() {
        return landSize;
    }

    public String getLocation() {
        return location;
    }

    public String getSoilType() {
        return soilType;
    }

    public List<Integer> getSubsidyApplications() {
        return subsidyApplications;
    }

    public List<Integer> getApprovedSubsidies() {
        return approvedSubsidies;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}