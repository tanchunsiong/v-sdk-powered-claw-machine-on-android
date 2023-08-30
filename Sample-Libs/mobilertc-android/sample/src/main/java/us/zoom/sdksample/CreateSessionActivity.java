package us.zoom.sdksample;

import us.zoom.sdksample.util.RandomUtil;

public class CreateSessionActivity extends BaseSessionActivity {

    protected String getDefaultSessionName() {
        String defaultName = super.getDefaultSessionName();
        return defaultName + "_" + RandomUtil.getEightRandom();
    }

    @Override
    protected void init() {
        super.init();
        setHeadTile(R.string.create_title);
    }

}
