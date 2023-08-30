package us.zoom.sdksample;

public class JoinSessionActivity extends BaseSessionActivity {

    protected String getDefaultSessionName() {
        return "";
    }

    @Override
    protected void init() {
        super.init();
        setHeadTile(R.string.join_title);
        btnJoin.setText(R.string.join);
    }

}
