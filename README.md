<img src="https://raw.githubusercontent.com/skrumblenetwork/cerebro/master/img/SKM_Logo_black.png" width="30%" height="30%">

# Pocketbook

Skrumble Network's Pocketbook is library for Android that allows you to create and interact with wallets, retrieve balances etc.

# Getting Started
### Installing

#### Gradle
Step 1. Add it in your root build.gradle at the end of repositories:
```java
allprojects {
  repositories {
    ...
  maven { url 'https://jitpack.io' }
  }
}
```

Step 2. Add the dependency
```java
dependencies {
  implementation 'com.github.skrumblenetwork:pocketbook:0.1.0'
  implementation 'com.android.support:multidex:1.0.3'
}
```
Step 3. Add following to your module gralde file:
```java
android{
......
  defaultConfig {
    .......
    multiDexEnabled true
  }
  
 packagingOptions{
    exclude 'lib/x86_64/darwin/libscrypt.dylib'
  }
}
```

### Configuration

```java
Call following statement in oncreate of application class
// Pass Contract Address for ERC20 Token
Config.init(BLOCK_CHAIN_ADDRESS, CONTRACT_ADDRESS);
```

### Usage

```java
WalletUtils.generateTwelveWord() // Generate twelve words
WalletUtils.createPrivateKey(words, "") // Generate Private Key
WalletUtils.getWalletAddress(privateKey) // Generate Wallet Address

 Web3jHandler instance = Web3jHandler.getInstance();
  instance.getBalance((success, object) -> {
      // Getting Balance     
  });
    
  instance.getErc20Balance((success, object) -> {
      // Getting Erc20 token Balance     
  });

```

# Contact

Have questions about our Github page?

Reach out to one of our team members in our main groups on [Ally](https://getally.io/c/) or [Telegram](https://t.me/skrumble) and be sure to follow us on [Twitter](https://twitter.com/SkrumbleNetwork).

For all other inquiries, please contact Shelby Pearce, Marketing Manager at Skrumble Network, at shelby@skrumble.com

# License

Usage is provided under the MIT License. See [LICENSE](./master/LICENSE) for the full details.
