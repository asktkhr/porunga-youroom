package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GroupActivity extends Activity {

	private final int DELETE_TOKEN = 1;
	private final int REACQUIRE_GROUP = 2;
	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);
	private YouRoomGroupAdapter adapter;
	private ProgressDialog progressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if( !youRoomUtil.isLogined() ){
            setContentView(R.layout.top);
        	Button login_button = (Button)findViewById(R.id.login_button);
        	
            OnClickListener loginClickListener = new OnClickListener(){
            	public void onClick(View v) {
                	if ( v.getId() == R.id.login_button){
                		Intent intent = new Intent(getApplication(), LoginActivity.class); 
                		startActivity(intent);
                	}
            	}		    	
            };
        	
        	login_button.setOnClickListener(loginClickListener);

        } else {
            setContentView(R.layout.group_view);
           	progressDialog = new ProgressDialog(this);
    		setProgressDialog(progressDialog);
    		progressDialog.show();
    		
    		ListView listView = (ListView)findViewById(R.id.listView1);
    		ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();    		
			GetGroupTask task = new GetGroupTask();
			task.execute();
    					
    		adapter = new YouRoomGroupAdapter(this, R.layout.group_list_item, dataList);
    		listView.setAdapter(adapter);
    		
    		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    	        @Override
    	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	            ListView listView = (ListView) parent;
    	            YouRoomGroup item = (YouRoomGroup) listView.getItemAtPosition(position);
    	            Intent intent = new Intent(getApplication(), RoomActivity.class);
    	            intent.putExtra("roomId", String.valueOf(item.getId()));
    	            intent.putExtra("group", item);
    	            startActivity(intent);
    	        }
    	    });
        }        
    }

	@Override
	public void onStart(){
		super.onStart();		
	}
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, DELETE_TOKEN, DELETE_TOKEN, R.string.delete_token);
      menu.add(Menu.NONE, REACQUIRE_GROUP, REACQUIRE_GROUP, R.string.reacquire_group);
    return super.onCreateOptionsMenu(menu);
    }

	public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        case DELETE_TOKEN:
        	if ( youRoomUtil.removeOauthTokenFromLocal() ){
        		Intent intent = new Intent(this, LoginActivity.class); 
        		startActivity(intent);
        	}
        	ret = true;
	    	break;
        case REACQUIRE_GROUP:
           	progressDialog = new ProgressDialog(this);
    		setProgressDialog(progressDialog);
    		progressDialog.show();
			GetGroupTask task = new GetGroupTask();
			task.execute();
			ret =true;
			break;
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }    
        return ret;
    }
    
    // ListView�J�X�^�}�C�Y�p��ArrayAdapter
	public class YouRoomGroupAdapter extends ArrayAdapter<YouRoomGroup> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomGroup> items;
		
		public YouRoomGroupAdapter( Context context, int textViewResourceId, ArrayList<YouRoomGroup> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.group_list_item, null);				
			}
			YouRoomGroup group = (YouRoomGroup)this.getItem(position);
			TextView name = null;
			TextView updateTime = null;
			
			if ( group != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				updateTime = (TextView)view.findViewById(R.id.textView2);
			}
			if ( name != null ){
				name.setText(group.getName());
			}
			if ( updateTime != null ){
				updateTime.setText(YouRoomUtil.convertDatetime(group.getUpdatedTime()));
			}

			int compareResult = YouRoomUtil.calendarCompareTo(group.getLastAccessTime(), group.getUpdatedTime());
			if ( group.getLastAccessTime() != null ){
				if ( compareResult < 0 ){
					updateTime.setTextColor(Color.RED);
				}
			}
			return view;
		}
	}
	
	private ArrayList<YouRoomGroup> getMyGroupList(){
		
		// input
		// output [YouRoomGroup...] or []
		
        YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    	String myGroups = youRoomCommand.getMyGroup();
  		ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
		
		try {
	    	JSONArray jsons = new JSONArray(myGroups);
	    	for(int i =0 ; i< jsons.length(); i++){
	    		YouRoomGroup group = new YouRoomGroup();
		    	JSONObject jObject = jsons.getJSONObject(i);
		    	JSONObject groupObject = jObject.getJSONObject("group");

		    	int id = groupObject.getInt("id");
		    	String name = groupObject.getString("name");
		    	
		    	String createdTime = groupObject.getString("created_at");
		    	String updatedTime = groupObject.getString("updated_at");

		    	group.setId(id);
		    	group.setName(name);
		    	group.setUpdatedTime(updatedTime);
		    	group.setCreatedTime(createdTime);
		    	
		    	//�b��I�ȃ`�F�b�N
		    	String lastAccessTime = youRoomUtil.getAccessTimeFromLocal(id);
		    	group.setLastAccessTime(lastAccessTime);
		    	String time = YouRoomUtil.getRFC3339FormattedTime();
		    	youRoomUtil.storeAccessTimeToLocal(id, time);
		    	
	    		dataList.add(group);
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
				
		return dataList;
	}

	public class GetGroupTask extends AsyncTask<Void, Void, ArrayList<YouRoomGroup>> {
				
		@Override
		protected ArrayList<YouRoomGroup> doInBackground(Void... ids) {						
			ArrayList<YouRoomGroup> dataList = getMyGroupList();
			return dataList;
		}
				
		@Override
		protected void onPostExecute(ArrayList<YouRoomGroup> dataList){
			Iterator iterator = dataList.iterator();
			while( iterator.hasNext() ) {
				adapter.add((YouRoomGroup) iterator.next());
			}
			adapter.notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}
	
	public void setProgressDialog(ProgressDialog progressDialog){
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("���������s�����Ă��܂�");
		progressDialog.setCancelable(true);
	}
	
}
