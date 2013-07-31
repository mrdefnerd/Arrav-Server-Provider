package org.rev317.api.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.parabot.environment.api.utils.Filter;
import org.rev317.accessors.Client;
import org.rev317.accessors.Scene;
import org.rev317.accessors.SceneObjectTile;
import org.rev317.accessors.SceneTile;
import org.rev317.api.wrappers.scene.SceneObject;
import org.rev317.loader.Loader;
/**
 * 
 * @author Clisprail
 *
 */
public final class SceneObjects {
	private static Client client = null;
	private static Scene scene = null;
	private static SceneTile[][] sceneTiles = null;
	
	private static final Comparator<SceneObject> NEAREST_SORTER = new Comparator<SceneObject>() {

		@Override
		public int compare(SceneObject n1,SceneObject n2) {
			return n1.distanceTo() - n2.distanceTo();
		}

	};
	
	private static final Filter<SceneObject> ALL_FILTER = new Filter<SceneObject>() {

		@Override
		public boolean accept(SceneObject object) {
			return true;
		}
		
	};
	
	/**
	 * Gets the most important scene objects in game which can be interacted with, filters out: 'walls, wall decorations, ground decorations'
	 * @return scene objects
	 */
	public static final SceneObject[] getSceneObjects(Filter<SceneObject> filter) {
		ArrayList<SceneObject> sceneObjects = new ArrayList<SceneObject>();
		if(client == null) {
			client = Loader.getClient();
		}
		scene = client.getScene();
		sceneTiles = scene.getSceneTiles()[client.getPlane()];
		for(int x = 0; x < 104; x++) {
			for(int y = 0; y < 104; y++) {
				final SceneObject sceneObjectAtTile = getSceneObjectAtTile(x, y, true);
				if(sceneObjectAtTile != null && filter.accept(sceneObjectAtTile)) {
					sceneObjects.add(sceneObjectAtTile);
				}
				
			}
		}
		return sceneObjects.toArray(new SceneObject[sceneObjects.size()]);
	}
	
	/**
	 * Gets the most important scene objects in game which can be interacted with
	 * @return scene objects
	 */
	public static final SceneObject[] getSceneObjects() {
		return getSceneObjects(ALL_FILTER);
	}
	
	/**
	 * Returns array of sceneobjects with the first index to be the nearest
	 * @param filter
	 * @return sceneobjects
	 */
	public static final SceneObject[] getNearest(Filter<SceneObject> filter) {
		final SceneObject[] objects = getSceneObjects(filter);
		Arrays.sort(objects, NEAREST_SORTER);
		return objects;
	}
	
	/**
	 * Returns array of sceneobjects with the first index to be the nearest
	 * @return sceneobjects
	 */
	public static final SceneObject[] getNearest() {
		return getNearest(ALL_FILTER);
	}
	
	/**
	 * Returns nearest objects with given id
	 * @param ids
	 * @return sceneobjects
	 */
	public static final SceneObject[] getNearest(final int... ids) {
		return getNearest(new Filter<SceneObject>() {

			@Override
			public boolean accept(SceneObject object) {
				for(final int id : ids) {
					if(id == object.getId()) {
						return true;
					}
				}
				return false;
			}
			
		});
	}
	
	private static SceneObject getSceneObjectAtTile(int x, int y, boolean useCached) {
		if(!useCached) {
			client = Loader.getClient();
			scene = client.getScene();
			sceneTiles = scene.getSceneTiles()[client.getPlane()];
		}
		final SceneTile sceneTile = sceneTiles[x][y];
		if(!client.isLoggedIn() || sceneTile == null) {
			return null;
		}
		final SceneObjectTile[] interactiveObjects = sceneTile.getInteractiveObjects();
		if(interactiveObjects != null) {
			for(final SceneObjectTile interactiveObject : interactiveObjects) {
				// get top
				if(interactiveObject != null) {
					return new SceneObject(interactiveObject, SceneObject.TYPE_INTERACTIVE);
				}
			}
		}
		SceneObjectTile sceneObjectTile = sceneTile.getWallObject();
		if(sceneObjectTile != null) {
			return new SceneObject(sceneObjectTile, SceneObject.TYPE_WALL);
		}
		return null;
	}

	/**
	 * Gets every loaded scene object in game
	 * @return every loaded scene object in game
	 */
	public static final SceneObject[] getAllSceneObjects() {
		ArrayList<SceneObject> sceneObjects = new ArrayList<SceneObject>();
		if(client == null) {
			client = Loader.getClient();
		}
		scene = client.getScene();
		sceneTiles = scene.getSceneTiles()[client.getPlane()];
		for(int x = 0; x < 104; x++) {
			for(int y = 0; y < 104; y++) {
				final Collection<SceneObject> sceneObjectsAtTile = getSceneObjectsAtTile(x, y, true);
				if(sceneObjectsAtTile != null && !sceneObjectsAtTile.isEmpty()) {
					sceneObjects.addAll(sceneObjectsAtTile);
				}
			}
		}
		return sceneObjects.toArray(new SceneObject[sceneObjects.size()]);
	}
	
	/**
	 * Gets all sceneobjects at a tile
	 * @param x
	 * @param y
	 * @param useCached
	 * @return array of sceneobjects, or null if there aren't any
	 */
	public static final Collection<SceneObject> getSceneObjectsAtTile(int x, int y, boolean useCached) {
		if(!useCached) {
			client = Loader.getClient();
			scene = client.getScene();
			sceneTiles = scene.getSceneTiles()[client.getPlane()];
		}
		final SceneTile sceneTile = sceneTiles[x][y];
		if(!client.isLoggedIn() || sceneTile == null) {
			return null;
		}
		ArrayList<SceneObject> sceneObjects = null;
		final SceneObjectTile[] interactiveObjects = sceneTile.getInteractiveObjects();
		if(interactiveObjects != null) {
			for(final SceneObjectTile interactiveObject : interactiveObjects) {
				if(interactiveObject != null) {
					if(sceneObjects == null) {
						sceneObjects = new ArrayList<SceneObject>();
					}
					sceneObjects.add(new SceneObject(interactiveObject, SceneObject.TYPE_INTERACTIVE));
				}
			}
		}
		SceneObjectTile sceneObjectTile = sceneTile.getWallObject();
		if(sceneObjectTile != null) {
			if(sceneObjects == null) {
				sceneObjects = new ArrayList<SceneObject>();
			}
			sceneObjects.add(new SceneObject(sceneObjectTile, SceneObject.TYPE_WALL));
		}
		
		sceneObjectTile = sceneTile.getWallDecoration();
		if(sceneObjectTile != null) {
			if(sceneObjects == null) {
				sceneObjects = new ArrayList<SceneObject>();
			}
			sceneObjects.add(new SceneObject(sceneObjectTile, SceneObject.TYPE_WALLDECORATION));
		}
		
		sceneObjectTile = sceneTile.getGroundDecoration();
		if(sceneObjectTile != null) {
			if(sceneObjects == null) {
				sceneObjects = new ArrayList<SceneObject>();
			}
			sceneObjects.add(new SceneObject(sceneObjectTile, SceneObject.TYPE_GROUNDDECORATION));
		}
		
		sceneObjectTile = sceneTile.getGroundItem();
		if(sceneObjectTile != null) {
			if(sceneObjects == null) {
				sceneObjects = new ArrayList<SceneObject>();
			}
			sceneObjects.add(new SceneObject(sceneObjectTile, SceneObject.TYPE_GROUNDITEM));
		}
		return sceneObjects;
	}

}
