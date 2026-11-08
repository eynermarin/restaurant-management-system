package restaurante.menu.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;

/**
 * Arma el contenido de un JTree con la forma Menú, luego Categoría, luego Opción.
 * Uso en la pantalla: arbolMenu.setModel(ArbolMenu.crearModelo(controlador.menuPorCategoria()));
 */
public class ArbolMenu {

    public static DefaultTreeModel crearModelo(LinkedHashMap<Categoria, ArrayList<OpcionMenu>> menu) {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Menú");
        for (Categoria categoria : menu.keySet()) {
            DefaultMutableTreeNode nodoCategoria = new DefaultMutableTreeNode(categoria);
            for (OpcionMenu opcion : menu.get(categoria)) {
                nodoCategoria.add(new DefaultMutableTreeNode(opcion));
            }
            raiz.add(nodoCategoria);
        }
        return new DefaultTreeModel(raiz);
    }
}
