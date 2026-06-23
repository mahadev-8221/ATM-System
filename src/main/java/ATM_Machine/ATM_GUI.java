package ATM_Machine;

import jakarta.persistence.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ATM_GUI extends JFrame implements ActionListener {

    JTextField amountField;
    JTextArea displayArea;

    JButton depositBtn, withdrawBtn, balanceBtn, historyBtn;

    EntityManager em;
    Account acc;

    ATM_GUI() {

        // DB setup
        em = JpaUtil.getEntityManager();

        acc = em.find(Account.class, 1);
        if (acc == null) {
            acc = new Account();
            acc.setId(1);
            acc.setBalance(1000);

            em.getTransaction().begin();
            em.persist(acc);
            em.getTransaction().commit();
        }

        // 🎨 Frame settings
        setTitle("ATM Dashboard");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setLayout(new BorderLayout(10, 10));

        // 🔝 Top Panel (Title)
        JLabel title = new JLabel("ATM MACHINE", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(title, BorderLayout.NORTH);

        // 🧾 Center Display
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setBackground(Color.BLACK);
        displayArea.setForeground(Color.GREEN);
        displayArea.setFont(new Font("Monospaced", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 🔽 Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(30, 30, 30));

        // Input Field
        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(amountField, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(new Color(30, 30, 30));

        depositBtn = createButton("Deposit");
        withdrawBtn = createButton("Withdraw");
        balanceBtn = createButton("Balance");
        historyBtn = createButton("History");

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(balanceBtn);
        buttonPanel.add(historyBtn);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setVisible(true);
    }

    // 🎨 Button Styling
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        double amount = 0;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (Exception ex) {}

        if (e.getSource() == depositBtn) {

            acc.setBalance(acc.getBalance() + amount);

            em.getTransaction().begin();
            em.merge(acc);

            Transaction t = new Transaction();
            t.setType("DEPOSIT");
            t.setAmount(amount);
            t.setBalance(acc.getBalance());
            em.persist(t);

            em.getTransaction().commit();

            displayArea.setText("Deposited: " + amount);
        }

        if (e.getSource() == withdrawBtn) {

            if (amount > acc.getBalance()) {
                displayArea.setText("Insufficient Balance!");
                return;
            }

            acc.setBalance(acc.getBalance() - amount);

            em.getTransaction().begin();
            em.merge(acc);

            Transaction t = new Transaction();
            t.setType("WITHDRAW");
            t.setAmount(amount);
            t.setBalance(acc.getBalance());
            em.persist(t);

            em.getTransaction().commit();

            displayArea.setText("Withdrawn: " + amount);
        }

        if (e.getSource() == balanceBtn) {
            displayArea.setText("Current Balance: " + acc.getBalance());
        }

        if (e.getSource() == historyBtn) {

            List<Transaction> list =
                    em.createQuery("from Transaction", Transaction.class).getResultList();

            StringBuilder sb = new StringBuilder("Transaction History:\n\n");

            for (Transaction t : list) {
                sb.append(t.getType())
                  .append(" | ")
                  .append(t.getAmount())
                  .append(" | Balance: ")
                  .append(t.getBalance())
                  .append("\n");
            }

            displayArea.setText(sb.toString());
        }

        amountField.setText("");
    }

    public static void main(String[] args) {
        new ATM_GUI();
    }
}