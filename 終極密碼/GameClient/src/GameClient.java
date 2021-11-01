import javax.swing.*;
import javax.swing.border.LineBorder;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class BackgroundPanel extends JPanel {//繼承面板
    private Image image = null;
    public BackgroundPanel(Image image) {
        this.image = image;
    }
    // 固定背景圖片，允許這個JPanel可以在圖片上新增其他元件
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(),this);//畫出跟他一樣的圖
    }
}

public class GameClient extends JApplet implements InterfaceConstants {
    private boolean myTurn = false;
    private Cell[][] cell = new Cell[10][10];
    private JLabel JTitle = new JLabel();
    private JLabel JStatus = new JLabel();
    private int rowSelect;
    private int columnSelect;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private boolean continueToPlay = true;
    private boolean waiting = true;
    private boolean isStandAlone = false;
    private String host = "127.0.1.1";
    private int player;

    public void init() {

        Image image=new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\R.jpg").getImage();
        JPanel Illustration=new BackgroundPanel(image);                          //創立含有背景的面板
        Illustration.setPreferredSize(new Dimension(90,this.getHeight()));//設定面板的寬高
        Image image2=new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\L.jpg").getImage();
        JPanel Illustration2=new BackgroundPanel(image2);
        Illustration2.setPreferredSize(new Dimension(90, this.getHeight()));

        JButton PCell = new JButton();

        PCell.setLayout(new GridLayout(10, 10, 0, 0));
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                PCell.add(cell[i][j] = new Cell(i, j));
                int n = j + 1 + (i * 10);
                String s = Integer.toString(n);
                cell[i][j].setText(s);

            }
        }
        PCell.setBorder(new LineBorder(Color.BLACK, 2));


        JTitle.setHorizontalAlignment(JLabel.CENTER);
        JTitle.setFont(new Font("標楷體", Font.BOLD, 60));//"標楷體"是真的標楷體 不是單純取名子，Font.BOLD是粗體字
        JTitle.setOpaque(true);
        JTitle.setBackground(Color.BLACK);
        JTitle.setForeground(Color.WHITE);
        JTitle.setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\Dn.png"));
        JTitle.setBorder(new LineBorder(Color.GRAY, 2));
        JStatus.setFont(new Font("新細明體", Font.BOLD, 36));
        JStatus.setBorder(new LineBorder(Color.GRAY, 2));
        JStatus.setOpaque(true);
        JStatus.setBackground(Color.BLACK);
        JStatus.setForeground(Color.GREEN);
        this.getContentPane().add(JTitle, BorderLayout.NORTH);
        this.getContentPane().add(PCell, BorderLayout.CENTER);
        this.getContentPane().add(JStatus, BorderLayout.SOUTH);

        this.getContentPane().add(Illustration,BorderLayout.EAST);
        this.getContentPane().add(Illustration2,BorderLayout.WEST);

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = null;
            if (isStandAlone)
                socket = new Socket(host, 9000);
            else
                socket = new Socket(getCodeBase().getHost(), 9000);//??????

            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
            /*System.out.println(socket);
            System.out.println(socket.getInetAddress().getHostAddress());*/

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
        Thread thread = new Thread(this::run);//※※
        thread.start();
    }

    public void run() {
        try {
            player = fromServer.readInt();//接收GameServer的第37行，即"new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);"
            if (player == PLAYER1) {
                JTitle.setText("玩家1");
                JStatus.setText("兩人開局,等待玩家2入場");
                fromServer.readInt();//接收Server端裡的HandleAsession的37行，那個隨便傳來的1(代表有玩家2加入了)
                JStatus.setText("玩家2加入,玩家1先猜");
                myTurn = true;
            } else if (player == PLAYER2) {
                JTitle.setText("玩家2");
                JStatus.setText("遊戲開始,玩家1先猜");
            }
            while (continueToPlay)//一開始是true
            {
                if (player == PLAYER1) {
                    waitForPlayerAction();//程式暫停1秒
                    sendMove_Num();//傳下手位置過去跟數字
                    receiveInfoFromServer();
                    receiveInfoFromServer();
                } else if (player == PLAYER2) {
                    receiveInfoFromServer();//接收對方下手位置跟數字
                    waitForPlayerAction();//如果猜錯再傳下手位置跟數字
                    sendMove_Num();//傳下手位置過去
                    receiveInfoFromServer();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(300);
        }
        waiting = true;
    }

    private void sendMove_Num() throws IOException {
        toServer.writeInt(rowSelect);//把滑鼠事件紀錄的行列傳出去(沒設定初值但滑鼠事件一定會先觸發)
        toServer.writeInt(columnSelect);
        toServer.writeInt(Integer.parseInt(cell[rowSelect][columnSelect].getText()));
    }

    private void receiveInfoFromServer() throws IOException {
        int status = fromServer.readInt();//接收戰局結果
        if (status == PLAYER1_WIN) {
            continueToPlay = false;
            if (player == PLAYER1) {
                cell[rowSelect][columnSelect].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                cell[rowSelect][columnSelect].setRolloverIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                JStatus.setText("哈哈，老天爺都幫我啊!");
                JTitle.setText("贏家");
            } else if (player == PLAYER2) {
                receiveMove();
                cell[rowSelect][columnSelect].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                cell[rowSelect][columnSelect].setRolloverIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                JStatus.setText("可惡!我輸了");
                JTitle.setText("輸家");
            }
        } else if (status == PLAYER2_WIN) {
            continueToPlay = false;
            if (player == PLAYER2) {
                //cell[rowSelect][columnSelect].setBackground(Color.YELLOW);
                cell[rowSelect][columnSelect].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                cell[rowSelect][columnSelect].setRolloverIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                JStatus.setText("怎麼樣，我猜到了吧!");
                JTitle.setText("贏家");
            } else if (player == PLAYER1) {
                receiveMove();
                //cell[rowSelect][columnSelect].setBackground(Color.YELLOW);
                cell[rowSelect][columnSelect].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                cell[rowSelect][columnSelect].setRolloverIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\K.jpg"));
                JStatus.setText("竟然先被你猜到了");
                JTitle.setText("輸家");
            }
        } else if (status == EQUAL)//由Server端isFull()成立後所傳過來的值
        {
            continueToPlay = false;
            JStatus.setText("遊戲結束，平手");
            if (player == PLAYER2)
                receiveMove();
        } else if (status == CONTINUE)//如果沒有結果，即比賽繼續
        {
            int result = fromServer.readInt();
            receiveMove();//接收對方下手位置並在自己的視窗上畫下他的標誌
            if (result == TOOSMALL)//接收太大或太小或猜對了
            {
                for (int i = 0; i < rowSelect; i++)
                    for (int j = 0; j < 10; j++)
                    { cell[rowSelect][i].setBackground(Color.GRAY);
                        cell[i][j].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
                for (int i = 0; i <= columnSelect; i++)
                {cell[rowSelect][i].setBackground(Color.GRAY);
                    cell[rowSelect][i].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
            } else if (result == TOOBIG)//接收太大或太小或猜對了
            {
                for (int i = columnSelect; i < 10; i++)
                {cell[rowSelect][i].setBackground(Color.GRAY);
                    cell[rowSelect][i].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
                for (int i = (rowSelect + 1); i < 10; i++)
                    for (int j = 0; j < 10; j++)
                    {cell[i][j].setBackground(Color.GRAY);
                        cell[i][j].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
            }
        } else if (status == TURNCONTINUE)//如果沒有結果，即比賽繼續
        {
            int result = fromServer.readInt();
            receiveMove();//接收對方下手位置並在自己的視窗上畫下他的標誌
            if (result == TOOSMALL)//接收太大或太小或猜對了
            {
                for (int i = 0; i < rowSelect; i++)
                    for (int j = 0; j < 10; j++)
                    {cell[i][j].setBackground(Color.GRAY);
                        cell[i][j].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
                for (int i = 0; i <= columnSelect; i++)
                {cell[rowSelect][i].setBackground(Color.GRAY);
                    cell[rowSelect][i].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
            } else if (result == TOOBIG)//接收太大或太小或猜對了
            {
                for (int i = columnSelect; i < 10; i++)
                {cell[rowSelect][i].setBackground(Color.GRAY);
                    cell[rowSelect][i].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
                for (int i = (rowSelect + 1); i < 10; i++)
                    for (int j = 0; j < 10; j++)
                    {cell[i][j].setBackground(Color.GRAY);
                        cell[i][j].setIcon(new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B2.jpg"));}
            }
            JStatus.setText("換我了");
            myTurn = true;
            //System.out.println("換2了");
        }
    }

    private void receiveMove() throws IOException {
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        rowSelect = row;
        columnSelect = column;
    }


    public class Cell extends JButton implements MouseListener {

        private int row;
        private int column;

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            setBorder(new LineBorder(Color.BLACK, 2));
            //setText(" ");
            //setOpaque(false);
            //setContentAreaFilled(false);
            //setBorderPainted(false);
            // 設置按鈕大小與圖片一致
            /*Dimension d = new Dimension(30, 30);
            this.setSize(d);
            this.setMaximumSize(d);
            this.setMinimumSize(d);*/
            //設置按鈕背景
            ImageIcon icon1 = new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B.jpg");
            setIcon(icon1);
            //設置滑鼠碰到按鈕時的背景
            ImageIcon icon2 = new ImageIcon("D:\\JAVA專案區\\Game\\UltimateCode\\GameClient\\src\\B(Black).jpg");
            setRolloverIcon(icon2);
            this.setHorizontalTextPosition(CENTER);     //設置文字在按鈕中的位置(水平置中)
            this.setVerticalTextPosition(CENTER);
            setBorderPainted(true);         //繪製邊框
            setFocusPainted(false);         //不繪製焦点
            setContentAreaFilled(false);    //不繪製內容區
            //setFocusable(true);設置焦點控制
            setMargin(new Insets(0, 0, 0, 0));//設置按鈕邊框與內容之間的像素
            Font font = new Font("Arial", Font.BOLD, 18);//設置字體
            setFont(font);
            setForeground(Color.BLUE);      //設置前景色（文字颜色）
            addMouseListener(this);
        }
        /* public char getToken()
         {
             return token;
         }*/


        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (myTurn && cell[row][column].getBackground()!=Color.GRAY) {
                myTurn = false;
                rowSelect = row;//紀錄列
                columnSelect = column;//紀錄行
                JStatus.setText("等待另一位玩家移動");
                waiting = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }



        public static void main(String[] args) {
            JFrame frame = new JFrame("終極密碼遊戲客戶端");
            //frame.getContentPane().setVisible(false);
            //frame.setUndecorated(true);
            //frame.setBackground(new Color(0,0,0,0));



            GameClient applet = new GameClient();//宣告了一個物件 叫applet
            applet.isStandAlone = true;
            if (args.length == 1)
                applet.host = args[0];
            frame.getContentPane().add(applet, BorderLayout.CENTER);

            applet.init();//呼叫init方法
            applet.start();//用start呼叫run方法
            //applet.run();
            frame.setSize(900, 865);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

        }


}
