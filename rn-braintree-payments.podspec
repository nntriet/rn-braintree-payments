require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "rn-braintree-payments"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  rn-braintree-payments
                   DESC
  s.homepage     = "https://github.com/nntriet/rn-braintree-payments"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "NNT" => "nntriet@gmail.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/nntriet/rn-braintree-payments.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # Braintree Drop-in
  s.dependency "BraintreeDropIn"
  # Braintree Cards and PayPal
  s.dependency "Braintree"
end

