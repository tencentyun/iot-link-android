## SDK发布前Checklist

1、修改工程[config.gradle](../config.gradle)中的sdkVersion

2、去[oss.sonatype.org](https://oss.sonatype.org/#stagingRepositories)上触发release操作进行SDK的正式版发布，操作如下：
* 点击`Refresh`
* 在列表出选中要release的对象
* 点击`Close`
* 等待下方`Activity`栏的任务执行完，点击`Release`
* 等待Release操作也执行完后，可以在[Maven Central](https://search.maven.org/)搜索SDK对应的artifactId，该网站有延迟可能查询不到；也可以在build.gradle中添加刚发布的正式版SDK，通过判断是否能引用到来判断SDK正式版是否成功release