package loginandsignup;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SchoolItemOrderingApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String selectedOrganization;
    private String selectedProduct;
    private double selectedProductPrice;
    private int selectedProductQuantity;
    private String currentCardName = "initialCard";  // Added field

    private JPanel panel;
    private JSpinner quantitySpinner;
    private JButton cartButton;
    private List<String> cartItems;
    private boolean cartOpened = false;
    private String userName;

    private JTable cartTable;

    public SchoolItemOrderingApp() {
        initializeUI();
        cartItems = new ArrayList<>();
    }

    void setUser(String fname) {
        this.userName = fname;
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        setTitle("CICS DEPARTMENT STUDENT MARKETPLACE - Welcome, " + userName + "!");
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        JPanel organizationPanel = new JPanel(new GridLayout(1, 2));
        organizationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton organizationAButton = createOrganizationButton("JPCS");
        organizationPanel.add(organizationAButton);

        JButton organizationBButton = createOrganizationButton("TECHIS");
        organizationPanel.add(organizationBButton);

        mainPanel.add(organizationPanel, "ORG_SELECTION");

        JPanel jpcsProductPanel = createProductPanel("JPCS");
        mainPanel.add(jpcsProductPanel, "JPCS_PRODUCTS");

        JPanel techisProductPanel = createProductPanel("TECHIS");
        mainPanel.add(techisProductPanel, "TECHIS_PRODUCTS");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton backButton = new JButton("BACK");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "ORG_SELECTION");
                currentCardName = "ORG_SELECTION";  // Modified this line

                // Add logic to return to the login interface when going back from the organization interface
                if (!"ORG_SELECTION".equals(currentCardName)) {
                    showLoginInterface();
                }
            }
        });
        buttonPanel.add(backButton);

        JButton exitButton = new JButton("EXIT");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cartButton = new JButton("Shopping Cart");
        cartButton.addActionListener(e -> {
            showProductDetails();
            updateCartButton();
        });
        cartPanel.add(cartButton);

        add(cartPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void showLoginInterface() {
        // Add logic to show the login interface
        Login loginFrame = new Login();
        loginFrame.setVisible(true);
        loginFrame.pack();
        loginFrame.setLocationRelativeTo(null); // center
        this.dispose(); // Close the current SchoolItemOrderingApp window
    }

    private JButton createOrganizationButton(String organizationName) {
        JButton button = new JButton(organizationName);
        button.addActionListener(e -> cardLayout.show(mainPanel, organizationName + "_PRODUCTS"));

        // Set accessibility description
        button.getAccessibleContext().setAccessibleDescription("Select products from " + organizationName);

        return button;
    }

    private JPanel createProductPanel(String organizationName) {
        JPanel productPanel = new JPanel(new GridLayout(2, 3));
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] products = {"T-Shirts", "Pens", "Stickers", "Tote Bags", "Pins", "Fans"};
        double[] prices = {300, 10, 20, 150, 30, 15};

        for (int i = 0; i < products.length; i++) {
            JButton productButton = new JButton(products[i]);
            final double price = prices[i];

            final int index = i;

            productButton.addActionListener(e -> {
                setSelectedProduct(organizationName, products[index], prices[index], 1);
                showProductDetails();
                updateCartButton();
            });

            productPanel.add(productButton);
        }

        return productPanel;
    }

    private void setSelectedProduct(String organization, String product, double price, int quantity) {
        selectedOrganization = organization;
        selectedProduct = product;
        selectedProductPrice = price;
        selectedProductQuantity = quantity;
    }

    private void showProductDetails() {
        panel = new JPanel(new GridLayout(5, 1));

        JLabel nameLabel = new JLabel("Product: " + selectedProduct);
        JLabel priceLabel = new JLabel("Price: " + selectedProductPrice);

        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        JLabel quantityLabel = new JLabel("Quantity:");

        panel.add(nameLabel);
        panel.add(priceLabel);
        panel.add(quantityLabel);
        panel.add(quantitySpinner);

        int result = JOptionPane.showOptionDialog(
                this,
                panel,
                "Product Details",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"Add to Cart", "Buy Now"},
                "Add to Cart"
        );

        if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
            int selectedQuantity = (int) quantitySpinner.getValue();
            String cartItem = selectedOrganization + ": " + selectedProduct + " (Quantity: " + selectedQuantity + ")";
            cartItems.add(cartItem);

            String actionMessage = (result == JOptionPane.YES_OPTION) ? "Added to Cart" : "Bought Now";
            JOptionPane.showMessageDialog(
                    this,
                    actionMessage + ": " + selectedProduct + " (Quantity: " + selectedQuantity + ")",
                    actionMessage,
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void updateCartButton() {
        int numItems = cartItems.size();
        double totalCost = calculateTotalCost();

        StringBuilder cartButtonText = new StringBuilder("Shopping Cart");

        if (numItems > 0) {
            cartButtonText.append(" (").append(numItems).append(" items)");
        }

        cartButton.setText(cartButtonText.toString());

        cartButton.addActionListener(e -> {
            if (!cartOpened) {
                viewCartContents();
                cartOpened = true;
            }
        });
    }

    private double calculateTotalCost() {
        double total = 0.0;

        for (String item : cartItems) {
            String[] parts = item.split(" ");
            String productName = parts[0];
            int quantity = Integer.parseInt(parts[parts.length - 1].replace(")", ""));
            double productPrice = getProductPrice(productName);

            double itemTotal = productPrice * quantity;
            total += itemTotal;
        }

        return total;
    }

    private double getProductPrice(String productName) {
        switch (productName) {
            case "T-Shirts":
                return 300;
            case "Pens":
                return 10;
            case "Stickers":
                return 20;
            case "Tote Bags":
                return 150;
            case "Pins":
                return 30;
            case "Fans":
                return 15;
            default:
                return 0;
        }
    }

    private void proceedToBuy(JPanel cartPanel) {
        JOptionPane.showMessageDialog(
                this,
                "Thank you for your purchase!",
                "Purchase Confirmation",
                JOptionPane.INFORMATION_MESSAGE
        );

        cartItems.clear();
        updateCartButton();
    }

    private void viewCartContents() {
        JPanel cartPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"ITEM (Quantity)", "PRICE", "TOTAL"};

        Object[][] rowData = new Object[cartItems.size()][3]; // Updated to 3 columns

        double total = 0.0;

        for (int i = 0; i < cartItems.size(); i++) {
            String item = cartItems.get(i);
            String[] parts = item.split(":");
            String organizationName = parts[0].trim();
            String productNameWithQuantity = parts[1].trim();
            int quantity = extractQuantity(item);
            double productPrice = getProductPrice(productNameWithQuantity.split("\\(")[0].trim());
            double itemTotal = productPrice * quantity;

            rowData[i][0] = organizationName + ": " + productNameWithQuantity + " (" + quantity + ")";
            rowData[i][1] = "Php" + productPrice;
            rowData[i][2] = "Php" + itemTotal;

            total += itemTotal;
        }

        JTable cartTable = new JTable(rowData, columnNames);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        cartPanel.add(scrollPane, BorderLayout.CENTER);

        cartPanel.add(new JLabel("Total Price: Php" + total), BorderLayout.SOUTH);

        int result = JOptionPane.showOptionDialog(
                this,
                cartPanel,
                "Shopping Cart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"Buy Now", "Cancel"},
                "Buy Now"
        );

        if (result == JOptionPane.YES_OPTION) {
            proceedToBuy(cartPanel);
        }

        cartOpened = false;
    }

    private JButton createActionButton(int rowIndex) {
        JButton button = new JButton("Remove");
        button.addActionListener(e -> {
            removeCartItem(rowIndex);
            viewCartContents();
        });

        // Set accessibility description
        button.getAccessibleContext().setAccessibleDescription("Remove item from the cart");

        return button;
    }

    private void removeCartItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cartItems.size()) {
            cartItems.remove(rowIndex);
            updateCartButton();
        }
    }

    private int extractQuantity(String cartItem) {
        String[] parts = cartItem.split(" ");
        String lastPart = parts[parts.length - 1];
        return Integer.parseInt(lastPart.replace(")", ""));
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;

        public ButtonEditor(JTextField textField, JFrame parent) {
            super(textField);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                int rowIndex = cartTable.convertRowIndexToModel(cartTable.getEditingRow());
                removeCartItem(rowIndex);
                viewCartContents();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchoolItemOrderingApp().setVisible(true));
    }
}
