//
//  ConverationViewController.m
//  AVOSCloud
//
//  Created by 小发工作室 on 2019/5/15.
//

#import "ConverationViewController.h"

@interface ConverationViewController ()

@end

@implementation ConverationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBar.barTintColor = [UIColor colorWithRed:253/255.0 green:216/255.0 blue:44/255.0 alpha:1];
    UIButton *leftCustomButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 35, 35)];
    [leftCustomButton setBackgroundImage:[UIImage imageNamed:@"back"] forState:UIControlStateNormal];
    [leftCustomButton addTarget:self action:@selector(back) forControlEvents:UIControlEventTouchUpInside];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:leftCustomButton];
    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];

}


- (void)back {
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
