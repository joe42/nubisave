package nubisave.component.graph.tranformer;

import org.apache.commons.collections15.Transformer;
import nubisave.component.graph.vertice.GenericNubiSaveComponent;

/**
 * Produce labels for GenericNubiSaveComponent instances from their {@link GenericNubiSaveComponent#getName() getName}
 * @param <V>
 */
public class GenericComponentLabeller<V> implements Transformer<V,String> {
	@Override
	public String transform(V vertex) {
		if(vertex instanceof GenericNubiSaveComponent)
			return ((GenericNubiSaveComponent)vertex).getName();
		else
			return null;
	}
}
