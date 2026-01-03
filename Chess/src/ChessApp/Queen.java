package ChessApp;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{

    //Since the queen behaves like a Castle and like a bishop, why not combine it into a single object?
    public Queen(int x, int y, String fileName, Turn colour) throws Exception {
        super(x, y, fileName, colour);
    }



    //use possible moves that can be calculated using the Bishop and the Tower Classes
    public List <List<Integer>> possibleMoves(){
        //Just copy the move finding function setup from tower and bishop classes

        allPossibleMoves.clear();

        //We now try to look for available spaces to the left
        this.setDirectionToConsider(new int[]{-1, 0});
        //up
        this.setDirectionToConsider(new int[]{0, -1});
        //right
        this.setDirectionToConsider(new int[]{1, 0});
        //down
        this.setDirectionToConsider(new int[]{0, 1});
        //up left
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
        return 'Q';
    }
}
