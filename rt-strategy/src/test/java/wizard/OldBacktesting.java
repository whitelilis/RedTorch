package wizard;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.redtorch.core.test.TestConfiguration;
import xyz.redtorch.core.zeus.BacktestingEngine;
import xyz.redtorch.core.zeus.BacktestingEngine.BacktestingSection;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.impl.BacktestingEngineImpl;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@PropertySource(value = { "classpath:rt-core.properties" })
public class OldBacktesting {

	@Autowired
	private ZeusDataService zeusDataService;

	@Value("${module.zeus.backtesting.output.dir}")
	private String backtestingOutputDir;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testStrategy() throws Exception {

		String strategyID = "wizard 113";
		boolean reloadStrategyEveryday = false;
		int backtestingDataMode = BacktestingEngine.DATA_MODE_TICK;
		List<BacktestingSection> backestingSectionList = new ArrayList<>();
		// 分段回测
		BacktestingSection backtestingSection = new BacktestingSection();
		backtestingSection.setStartDate("20170401");
		backtestingSection.setEndDate("20170405");

		String aim = "rb1710.SHFE";

		backtestingSection.addAliasRtSymbol("rb", aim);

		backtestingSection.addSubscribeRtSymbol("backTest", aim);

		backestingSectionList.add(backtestingSection);

		BacktestingEngine backtestingEngine = new BacktestingEngineImpl(zeusDataService, strategyID,
				backestingSectionList, backtestingDataMode, reloadStrategyEveryday, backtestingOutputDir);
		backtestingEngine.runBacktesting();
	}
}
