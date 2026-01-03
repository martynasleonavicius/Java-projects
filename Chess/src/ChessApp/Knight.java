package ChessApp;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece{

    public Knight(int x, int y, String fileName, Turn colour) throws Exception {
        super(x, y, fileName, colour);
    }

    public List <List<Integer>> possibleMoves(){
        allPossibleMoves.clear();
        //Find available moves for a horse
        int limit = 2;
        setDirectionToConsider(new int[]{-2, -1}, limit);
        setDirectionToConsider(new int[]{-1, -2}, limit);
        setDirectionToConsider(new int[]{1, -2}, limit);
        setDirectionToConsider(new int[]{2, -1}, limit);
        setDirectionToConsider(new int[]{-2, 1}, limit);
        setDirectionToConsider(new int[]{-1, 2}, limit);
        setDirectionToConsider(new int[]{2, 1}, limit);
        setDirectionToConsider(new int[]{1, 2}, limit);
        return allPossibleMoves;
    }

    public char returnClass(){
        return 'Z';
    }
}
