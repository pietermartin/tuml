package org.tuml.runtime.adaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tuml.runtime.domain.CompositionNode;


public class TransactionThreadEntityVar {
	
	private TransactionThreadEntityVar() {
	}

	private static ThreadLocal<Map<String, CompositionNode>> transactionEntityVar = new ThreadLocal<Map<String, CompositionNode>>() {

		Map<String, CompositionNode> newVertexMap = new HashMap<String, CompositionNode>();
		
		public Map<String, CompositionNode> get() {
			return newVertexMap;
		}

		public void set(Map<String, CompositionNode> newValue) {
			newVertexMap = newValue;
		}
		
		@Override
	    protected Map<String, CompositionNode> initialValue() {
	        return new HashMap<String, CompositionNode>();
	    }

	};

	public static boolean hasNoAuditEntry(String clazzAndId) {
		Map<String, CompositionNode> newVertexMap = transactionEntityVar.get();
		CompositionNode newVertex = newVertexMap.get(clazzAndId);
		return newVertex==null;
	}
	
	public static void clear() {
		transactionEntityVar.get().clear();
	}

	public static void setNewEntity(CompositionNode node) {
		transactionEntityVar.get().put(((CompositionNode)node).getVertex().getId().toString(), node);
	}
	
	public static List<CompositionNode> get() {
		return new ArrayList<CompositionNode>(transactionEntityVar.get().values());
	}
	
	public static CompositionNode remove(String key) {
		return transactionEntityVar.get().remove(key);
	}	
	

}