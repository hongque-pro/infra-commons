
dependencies {
  api(project(":sink-core"))
  api ("org.elasticsearch.client:transport") {
//    exclude group: "org.elasticsearch.client", module: "elasticsearch-rest-client"
  }
  api("org.apache.commons:commons-pool2")
}
