package com.hitop.service;

import java.io.File;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
  final Logger logger = LoggerFactory.getLogger(WalletService.class);
  
  private final WalletAppKit kit;
  private final NetworkParameters params;
  
  public WalletService(
      final @Value("${wallet.filename.prefix}") String filePrefix) throws Exception {
    
    // log output more compact and easily read, especially when using the JDK log adapter.
    BriefLogFormatter.init();

    // Figure out which network we should connect to. Each one gets its own set of files.
    //    if (args.length == 1 && args[1].equals("mainnet")) {
    //      params = MainNetParams.get();
    //      filePrefix = "monitor-service-mainnet";
    //    } else {
    params = TestNet3Params.get();
    //    }

    // Start up a basic app using a class that automates some boilerplate.
    kit = new WalletAppKit(params, new File("."), filePrefix) {
      @Override
      protected void onSetupCompleted() {
        // Don't make the user wait for confirmations for now, as the intention is they're sending it
        // their own money!
        kit.wallet().allowSpendingUnconfirmedTransactions();
      }
    };

    // Download the block chain and wait until it's done.
    kit.startAsync();
    kit.awaitRunning();
  }

  public void monitorReceiveEvent(final CoinsReceivedService coinsReceivedService) throws Exception {
    kit.wallet().addCoinsReceivedEventListener(coinsReceivedService);
  }

  public String getSendToAddress() {
    return LegacyAddress.fromKey(params, kit.wallet().currentReceiveKey()).toString();
  }
}