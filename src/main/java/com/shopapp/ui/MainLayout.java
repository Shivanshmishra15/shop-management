package com.shopapp.ui;

import com.shopapp.Main;
import com.shopapp.models.User;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainLayout extends BorderPane {

    private final StackPane contentArea;
    private final User currentUser;
    private Button activeNavButton = null;
    private Button btnLogout;

    public MainLayout(User user) {
        this.currentUser = user;
        
        // Create Sidebar
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);

        // Create Sidebar Header with Toggle
        HBox sidebarHeader = new HBox(15);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        sidebarHeader.setPadding(new Insets(0, 0, 30, 0));

        Button btnTheme = new Button();
        btnTheme.getStyleClass().add("theme-toggle");
        btnTheme.setTooltip(new Tooltip("Toggle Dark/Light Mode"));
        btnTheme.setOnAction(e -> {
            if (this.getStyleClass().contains("dark-mode")) {
                this.getStyleClass().remove("dark-mode");
            } else {
                this.getStyleClass().add("dark-mode");
            }
        });

        Label title = new Label("Shop Manager");
        title.getStyleClass().add("sidebar-title");
        title.setPadding(Insets.EMPTY); // Remove old padding
        
        sidebarHeader.getChildren().addAll(btnTheme, title);
        
        Label userLabel = new Label("User: " + user.getUsername());
        userLabel.getStyleClass().add("user-label");

        Button btnDashboard = createNavButton("Dashboard");
        Button btnBilling = createNavButton("Billing");
        Button btnInventory = createNavButton("Inventory");
        Button btnSuppliers = createNavButton("Suppliers");
        Button btnOrders = createNavButton("Purchase Orders");
        Button btnCustomers = createNavButton("Customers");
        Button btnReports = createNavButton("Reports");

        sidebar.getChildren().add(sidebarHeader);
        sidebar.getChildren().add(userLabel);
        
        sidebar.getChildren().add(btnDashboard);
        sidebar.getChildren().add(btnBilling);
        sidebar.getChildren().add(btnCustomers); // Available to all, or restrict? Let's make it available.
        
        // RBAC Logic
        if (!"CASHIER".equalsIgnoreCase(user.getRole())) {
            sidebar.getChildren().add(btnInventory);
            sidebar.getChildren().add(btnSuppliers);
            sidebar.getChildren().add(btnOrders); // Add PO Button
        }
        
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            sidebar.getChildren().add(btnReports);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        btnLogout = createNavButton("Logout");
        btnLogout.getStyleClass().add("logout-button");
        btnLogout.setOnAction(e -> handleLogout());
        sidebar.getChildren().addAll(spacer, btnLogout);

        // Create Content Area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        
        // Assemble Layout
        setLeft(sidebar);
        setCenter(contentArea);
        
        // Navigation Logic
        btnDashboard.setOnAction(e -> {
            setActiveButton(btnDashboard);
            btnLogout.setVisible(true);
            setView(new DashboardView(target -> handleDashboardNavigation(target, btnInventory)));
        });
        
        btnBilling.setOnAction(e -> {
            setActiveButton(btnBilling);
            btnLogout.setVisible(true);
            setView(new BillingView());
        });
        
        btnInventory.setOnAction(e -> {
            setActiveButton(btnInventory);
            btnLogout.setVisible(true);
            setView(new InventoryView(false));
        });
        
        btnSuppliers.setOnAction(e -> {
            setActiveButton(btnSuppliers);
            btnLogout.setVisible(false);
            setView(new SuppliersView());
        });
        
        btnOrders.setOnAction(e -> {
            setActiveButton(btnOrders);
            btnLogout.setVisible(true);
            setView(new PurchaseOrderView());
        });
        
        btnCustomers.setOnAction(e -> {
            setActiveButton(btnCustomers);
            btnLogout.setVisible(true);
            setView(new CustomersView());
        });
        
        btnReports.setOnAction(e -> {
            setActiveButton(btnReports);
            btnLogout.setVisible(true);
            setView(new FinanceView());
        });
        
        // Default View: Dashboard
        btnDashboard.fire();

        // Global Keyboard Shortcuts
        this.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case DIGIT1: btnDashboard.fire(); break;
                    case DIGIT2: btnBilling.fire(); break;
                    case DIGIT3: btnCustomers.fire(); break;
                    case DIGIT4: if (btnInventory.getParent() != null) btnInventory.fire(); break;
                    case DIGIT5: if (btnSuppliers.getParent() != null) btnSuppliers.fire(); break;
                    case DIGIT6: if (btnOrders.getParent() != null) btnOrders.fire(); break;
                    case DIGIT7: if (btnReports.getParent() != null) btnReports.fire(); break;
                    case L: handleLogout(); break;
                }
            }
        });
    }
    
    // Handle navigation from Dashboard
    private void handleDashboardNavigation(String target, Button inventoryBtn) {
        switch (target) {
            case "INVENTORY":
                if (inventoryBtn.getParent() != null) inventoryBtn.fire(); 
                break;
            case "INVENTORY_LOW":
                if (inventoryBtn.getParent() != null) {
                    setActiveButton(inventoryBtn);
                    setView(new InventoryView(true));
                }
                break;
            case "REPORTS":
                 setView(new Label("Coming Soon: Detailed Reports"));
                break;
        }
    }
    
    private void handleLogout() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
        try {
            new Main().start(new Stage());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        activeNavButton = btn;
        activeNavButton.getStyleClass().add("active");
    }

    private void setView(javafx.scene.Node node) {
        contentArea.getChildren().clear();
        node.setOpacity(0);
        contentArea.getChildren().add(node);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
