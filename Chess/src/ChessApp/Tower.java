package ChessApp;

import java.util.ArrayList;
import java.util.List;

public class Tower extends Piece{

    //Save the state in case Castling is needed.
    private boolean firstMove;

    public Tower(int x, int y, String fileName, Turn colour) throws Exception {
        super(x, y, fileName, colour);
        firstMove = true;
    }

    //We modify the move() superclass function so that castling function could be included
    //All we want to do is record if it is the first time the king moves
    @Override
    public void move(int newX, int newY){
        super.move(newX, newY);
        firstMove = false;
    }

    //Implemented for castling.
    public boolean getIsFirstMove(){
        return firstMove;
    }

    public List <List<Integer>> possibleMoves(){
        //Clear old possible moves when the piece is selected again
        allPossibleMoves.clear();
        //We now try to look for available spaces to the left
        this.setDirectionToConsider(new int[]{-1, 0});
        //up
        this.setDirectionToConsider(new int[]{0, -1});
        //right
        this.setDirectionToConsider(new int[]{1, 0});
        //down
        this.setDirectionToConsider(new int[]{0, 1});

        return super.allPossibleMoves;
    }

    public char returnClass(){
        return 'R';
    }
}
