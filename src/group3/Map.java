package group3;

import ontologies.Position;

public class Map {
    // Dimensions of the map
    protected int n,m;

    MapElement[][] map;
    public Map(int n, int m){
        this.n = n;
        this.m = m;
        this.map = new MapElement[n][m];
        for (int i=0; i<n; i++){
            for (int j=0; j<m; j++){
                this.map[i][j] = new MapElement(MapElement.ElementType.EMPTY);
            }
        }


        //TODO: populate map
    }

    public MapElement getMapElement(Position pos){
        return map[pos.getX()][pos.getY()];
    }
}
