import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HandleAsession extends Thread implements InterfaceConstants
{
    private int answer=(int)(Math.random()*100+1);
    private Socket player1;
    private Socket player2;
    private int[][] cell=new int[10][10];
    private DataInputStream fromPlayer1;//來至玩家1的訊息接口
    private DataOutputStream toPlayer1;//傳給玩家1的訊息接口
    private DataInputStream fromPlayer2;//來至玩家2的訊息接口
    private DataOutputStream toPlayer2;//傳給玩家2的訊息接口
    private boolean continueToplay=true;
    public HandleAsession(Socket player1,Socket player2)
    {
        this.player1=player1;
        this.player2=player2;
        for(int i=0;i<10;i++)
        {
            for(int j=0;j<10;j++)
            {
                cell[i][j]=j+1+(i*10);
            }
        }
    }
    public void run()
    {
        try
        {
            fromPlayer1=new DataInputStream(player1.getInputStream());
            toPlayer1=new DataOutputStream(player1.getOutputStream());
            fromPlayer2=new DataInputStream(player2.getInputStream());
            toPlayer2=new DataOutputStream(player2.getOutputStream());
            toPlayer1.writeInt(1);//此處的1沒有任何意思，就是隨便傳東西給玩家1讓他知道有玩家2加入了
            int count=0;
            while (true)
            {
                int row=fromPlayer1.readInt();
                int column=fromPlayer1.readInt();
                int num=fromPlayer1.readInt();
                count=1;
                if(isWin(num) && count==1)
                {
                    toPlayer1.writeInt(PLAYER1_WIN);
                    toPlayer2.writeInt(PLAYER1_WIN);
                    sendMove(toPlayer2,row,column);
                    break;
                } else {
                    toPlayer1.writeInt(CONTINUE);
                    toPlayer2.writeInt(TURNCONTINUE);
                    if(num<answer)
                    {
                        toPlayer1.writeInt(TOOSMALL);
                        toPlayer2.writeInt(TOOSMALL);
                    }
                    else if(num>answer)
                    {
                        toPlayer1.writeInt(TOOBIG);
                        toPlayer2.writeInt(TOOBIG);
                    }
                    sendMove(toPlayer2,row,column);
                    sendMove(toPlayer1,row,column);
                }


                row=fromPlayer2.readInt();
                column=fromPlayer2.readInt();
                num=fromPlayer2.readInt();
                count=2;
                if(isWin(num) && count==2)
                {
                    toPlayer1.writeInt(PLAYER2_WIN);
                    toPlayer2.writeInt(PLAYER2_WIN);
                    sendMove(toPlayer1,row,column);

                    break;
                }else{
                    toPlayer1.writeInt(TURNCONTINUE);
                    toPlayer2.writeInt(CONTINUE);
                    if(num<answer)
                    {
                        toPlayer1.writeInt(TOOSMALL);
                        toPlayer2.writeInt(TOOSMALL);
                    }
                    else if(num>answer)
                    {
                        toPlayer1.writeInt(TOOBIG);
                        toPlayer2.writeInt(TOOBIG);
                    }
                    sendMove(toPlayer2,row,column);
                    sendMove(toPlayer1,row,column);

                }
            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
    private void sendMove(DataOutputStream out,int row,int colum)throws IOException
    {
        out.writeInt(row);
        out.writeInt(colum);
    }
    private void sendResult(DataOutputStream out,int num)throws IOException
    {
        if(num<answer)
            out.writeInt(5);
        else if(num>answer)
            out.writeInt(6);
        else
            out.writeInt(7);
    }

    private boolean isWin(int num)
    {
        if(num==answer)
            return true;
        return false;
    }
}
