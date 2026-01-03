package ChessApp;

import java.util.ArrayList;
import java.util.List;


//The method structure created in the Tower class is useful here.
public class Bishop extends Piece{


    public Bishop(int x, int y, String fileName, Turn colour) throws Exception {
        super(x, y, fileName, colour);
    }

    public List <List<Integer>> possibleMoves(){

        //Clear old possible moves when the piece is selected again
        allPossibleMoves.clear();
        //Let us check if there are possible moves along any of the axis
        //We now try to look for available spaces up left
        this.setDirectionToConsider(new int[]{-1, -1});
        //up right
        this.setDirectionToConsider(new int[]{1, -1});
        //down right
        this.setDirectionToConsider(new int[]{1, 1});
        //down left
        this.setDirectionToConsider(new int[]{-1, 1});

        return allPossibleMoves;
    }

    public char returnClass(){
        return 'B';
    }
}
