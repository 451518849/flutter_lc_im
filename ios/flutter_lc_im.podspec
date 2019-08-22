#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'flutter_lc_im'
  s.version          = '0.2.0'
  s.summary          = 'A LeanCloud IM flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'https://github.com/451518849/flutter_lc_im'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'xiaofwang' => '451518849@qq.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.resource     = 'Assets/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'FakeChatKit','2.3.12'
  s.dependency 'TWMessageBarManager', '1.8.1'

  s.ios.deployment_target = '8.0'
end

