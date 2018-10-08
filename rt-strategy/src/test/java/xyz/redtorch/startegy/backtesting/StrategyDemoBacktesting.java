package xyz.redtorch.startegy.backtesting;

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
public class StrategyDemoBacktesting {

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
		backtestingSection.setStartDate("20181008");
		backtestingSection.setEndDate("20181009");

		String aim = "rb1901.SHFE";

		backtestingSection.addAliasRtSymbol("rb", aim);

		backtestingSection.addSubscribeRtSymbol("9999.sn.187.10000", aim);
		//backtestingSection.addSubscribeRtSymbol("9999.sn.187.10000", aim);
		//backtestingSection.addSubscribeRtSymbol("9999.sn724.187.10030", aim);
		/*
		backtestingSection.addAliasRtSymbol("IC", "IC1803.CFFEX");
		backtestingSection.addAliasRtSymbol("IH", "IH1803.CFFEX");

		backtestingSection.addSubscribeRtSymbol("9999.724SN02.187.10030", "IF1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IC1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IH1803.CFFEX");
		*/

		backestingSectionList.add(backtestingSection);

		BacktestingEngine backtestingEngine = new BacktestingEngineImpl(zeusDataService, strategyID,
				backestingSectionList, backtestingDataMode, reloadStrategyEveryday, backtestingOutputDir);
		backtestingEngine.runBacktesting();
	}
}
